package com.flex.interpre.domain.interview.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flex.interpre.domain.document.entity.Document;
import com.flex.interpre.domain.document.repository.DocumentRepository;
import com.flex.interpre.domain.interview.dto.response.*;
import com.flex.interpre.domain.interview.entity.Competency;
import com.flex.interpre.domain.interview.entity.Interview;
import com.flex.interpre.domain.interview.entity.InterviewChat;
import com.flex.interpre.domain.interview.entity.InterviewSession;
import com.flex.interpre.domain.interview.exception.InterviewExceptions;
import com.flex.interpre.domain.interview.repository.InterviewRepository;
import com.flex.interpre.domain.interview.repository.InterviewSessionRepository;
import com.flex.interpre.domain.jobSeeker.entity.JobSeeker;
import com.flex.interpre.domain.jobSeeker.repository.JobSeekerRepository;
import com.flex.interpre.global.property.BedrockProperty;
import com.flex.interpre.global.property.ClovaProperty;
import com.flex.interpre.global.util.PromptLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewService {

    private final InterviewSessionRepository interviewSessionRepository;
    private final InterviewRepository interviewRepository;
    private final JobSeekerRepository jobSeekerRepository;
    private final ClovaProperty clovaProperty;
    private final WebClient webClient;
    private final BedrockRuntimeClient bedrockRuntimeClient;
    private final BedrockProperty bedrockProperty;
    private final ObjectMapper objectMapper;
    private final DocumentRepository documentRepository;
    private final PromptLoader promptLoader;


    @Transactional
    public SessionResponse getSessionResponse(JobSeeker jobSeeker, UUID documentId) {

        Document document = documentRepository.findById(documentId)
                .orElseThrow(InterviewExceptions.DOCUMENT_NOT_FOUND::toException);

        Interview interview = interviewRepository.save(Interview.builder()
                .jobSeeker(jobSeeker)
                .build());

        InterviewSession interviewSession = interviewSessionRepository.save(InterviewSession.builder()
                .jobSeekerId(jobSeeker.getId())
                .interviewId(interview.getId())
                .contentText(document.getContentText())
                .ttl(1)
                .build());
        return new SessionResponse(interviewSession.getId());
    }

    public boolean isAnswerComplete(String currentQuestion, String currentAnswer) {

        if (currentAnswer == null || currentAnswer.trim().length() < 10) {
            return false;
        }

        String systemPrompt = promptLoader.getAnswerCompletionCheckPrompt();

        List<Map<String, Object>> messages = List.of(
                Map.of(
                        "role", "user",
                        "content", String.format("""
                                질문: %s
                                답변: %s

                                위 답변이 완료되었나요?
                                """, currentQuestion, currentAnswer)
                )
        );

        Map<String, Object> payloadMap = Map.of(
                "anthropic_version", "bedrock-2023-05-31",
                "max_tokens", 10,
                "temperature", 0.1,
                "system", systemPrompt,
                "messages", messages
        );

        try {
            String payload = objectMapper.writeValueAsString(payloadMap);

            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId(bedrockProperty.getModelId())
                    .body(SdkBytes.fromString(payload, StandardCharsets.UTF_8))
                    .contentType("application/json")
                    .accept("application/json")
                    .build();

            InvokeModelResponse response = bedrockRuntimeClient.invokeModel(request);

            String responseBody = response.body().asUtf8String();
            JsonNode jsonResponse = objectMapper.readTree(responseBody);

            String result = jsonResponse
                    .get("content")
                    .get(0)
                    .get("text")
                    .asText()
                    .trim()
                    .toUpperCase();

            return result.contains("COMPLETE") && !result.contains("INCOMPLETE");

        } catch (Exception e) {
            log.error("답변 완료 판단 실패: {}", e.getMessage(), e);
            return false;
        }
    }

    private String decideFollowupStrategy(List<InterviewChat> chatHistory) {
        String systemPrompt = promptLoader.getFollowupDecisionPrompt();

        int startIdx = Math.max(0, chatHistory.size() - 3);
        List<InterviewChat> recentChats = chatHistory.subList(startIdx, chatHistory.size());

        StringBuilder conversationContext = new StringBuilder();
        conversationContext.append("=== 최근 대화 내역 ===\n\n");

        for (int i = 0; i < recentChats.size(); i++) {
            InterviewChat chat = recentChats.get(i);
            conversationContext.append(String.format("Q%d: %s\n", i + 1, chat.getQuestion()));
            conversationContext.append(String.format("A%d: %s\n\n", i + 1,
                    chat.getAnswer() != null ? chat.getAnswer() : "(답변 없음)"));
        }

        List<Map<String, Object>> messages = List.of(
                Map.of("role", "user", "content", conversationContext.toString())
        );

        Map<String, Object> payloadMap = Map.of(
                "anthropic_version", "bedrock-2023-05-31",
                "max_tokens", 20,
                "temperature", 0.3,
                "system", systemPrompt,
                "messages", messages
        );

        try {
            String payload = objectMapper.writeValueAsString(payloadMap);

            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId(bedrockProperty.getModelId())
                    .body(SdkBytes.fromString(payload, StandardCharsets.UTF_8))
                    .contentType("application/json")
                    .accept("application/json")
                    .build();

            InvokeModelResponse response = bedrockRuntimeClient.invokeModel(request);

            String responseBody = response.body().asUtf8String();
            JsonNode jsonResponse = objectMapper.readTree(responseBody);

            String decision = jsonResponse
                    .get("content")
                    .get(0)
                    .get("text")
                    .asText()
                    .trim()
                    .toUpperCase();

            log.info("꼬리질문 판단 결과: {}", decision);

            if (decision.contains("FOLLOWUP")) {
                return "이전 답변을 바탕으로 더 깊이 있는 꼬리질문을 하세요. 기술적 세부사항, 문제 해결 과정, 트레이드오프 등을 물어보세요.";
            } else {
                return "자기소개서의 다른 경험이나 프로젝트로 주제를 전환하세요. 아직 다루지 않은 흥미로운 내용을 선택해주세요.";
            }

        } catch (Exception e) {
            log.error("꼬리질문 판단 실패, 기본 전략 사용: {}", e.getMessage());
            return "자기소개서의 다른 경험이나 프로젝트로 주제를 전환하세요.";
        }
    }

    public String generateQuestions(String document, List<InterviewChat> chatHistory) {

        String systemPrompt = promptLoader.getQuestionGenerationSystemPrompt();
        List<Map<String, Object>> messages = new ArrayList<>();

        if (chatHistory.isEmpty()) {
            String firstPrompt = promptLoader.getQuestionGenerationFirstPrompt(document);
            messages.add(Map.of("role", "user", "content", firstPrompt));
        } else {
            for (InterviewChat chat : chatHistory) {
                messages.add(Map.of("role", "assistant", "content", chat.getQuestion()));

                if (chat.getAnswer() != null && !chat.getAnswer().isBlank()) {
                    messages.add(Map.of("role", "user", "content", chat.getAnswer()));
                }
            }

            String strategy = decideFollowupStrategy(chatHistory);

            String followupPrompt = promptLoader.getQuestionGenerationFollowupPrompt(strategy, document);
            messages.add(Map.of("role", "user", "content", followupPrompt));
        }

        Map<String, Object> payloadMap = Map.of(
                "anthropic_version", "bedrock-2023-05-31",
                "max_tokens", 400,
                "temperature", 0.7,
                "system", systemPrompt,
                "messages", messages
        );

        try {

            String payload = objectMapper.writeValueAsString(payloadMap);

            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId(bedrockProperty.getModelId())
                    .body(SdkBytes.fromString(payload, StandardCharsets.UTF_8))
                    .contentType("application/json")
                    .accept("application/json")
                    .build();

            InvokeModelResponse response = bedrockRuntimeClient.invokeModel(request);

            String responseBody = response.body().asUtf8String();
            JsonNode jsonResponse = objectMapper.readTree(responseBody);

            return jsonResponse
                    .get("content")
                    .get(0)
                    .get("text")
                    .asText();

        } catch (Exception e) {
            throw InterviewExceptions.Parsing_Failed.toException();
        }
    }

    public byte[] tts(String text) {

        try {
            return webClient.post()
                    .uri(clovaProperty.getTtsUrl())
                    .header("X-NCP-APIGW-API-KEY-ID", clovaProperty.getTtsClientId())
                    .header("X-NCP-APIGW-API-KEY", clovaProperty.getTtsSecret())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData("speaker", "nara_call")
                            .with("text", text)
                            .with("format", "wav")
                            .with("sampling-rate", "24000"))
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .timeout(Duration.ofSeconds(30))
                    .block();
        } catch (Exception e) {
            throw InterviewExceptions.TTS_PROCESSING_FAILED.toException();
        }
    }

    @Transactional
    public List<InterviewHistory> getInterviewHistories(JobSeeker jobSeeker) {

        JobSeeker jobSeekerWithInterviews = jobSeekerRepository.findByIdWithInterviews(jobSeeker.getId());
        List<Interview> interviews = jobSeekerWithInterviews.getInterviews();
        return interviews.stream().map(InterviewHistory::from).toList();
    }

    @Transactional
    public InterviewDetailResponse getInterviewHistoryDetail(Interview interview) {

        return InterviewDetailResponse.builder()
                .id(interview.getId())
                .createdAt(interview.getCreatedAt())
                .title(interview.getTitle())
                .durationSeconds(interview.getDurationSecond())
                .qna(interview.getQnas().stream().map(QnaDto::from).toList())
                .build();
    }

    @Transactional
    public InterviewDetailResponse updateInterviewTitle(Interview interview, String title) {

        interview.setTitle(title);
        Interview updatedInterview = interviewRepository.save(interview);

        return InterviewDetailResponse.builder()
                .id(updatedInterview.getId())
                .createdAt(updatedInterview.getCreatedAt())
                .title(updatedInterview.getTitle())
                .durationSeconds(updatedInterview.getDurationSecond())
                .qna(updatedInterview.getQnas().stream().map(QnaDto::from).toList())
                .build();
    }

    @Transactional
    public InterviewAnalysisResult analyzeInterview(String fullTranscript) {

        String systemPrompt = promptLoader.getAnalysisPrompt();

        List<Map<String, Object>> messages = List.of(
                Map.of(
                        "role", "user",
                        "content", """
                                다음 면접 내용을 분석해주세요:
                                
                                [면접 내용]
                                %s
                                """.formatted(fullTranscript)
                )
        );

        Map<String, Object> payloadMap = Map.of(
                "anthropic_version", "bedrock-2023-05-31",
                "max_tokens", 2000,
                "temperature", 0.3,
                "system", systemPrompt,
                "messages", messages
        );

        try {
            String payload = objectMapper.writeValueAsString(payloadMap);

            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId(bedrockProperty.getModelId())
                    .body(SdkBytes.fromString(payload, StandardCharsets.UTF_8))
                    .contentType("application/json")
                    .accept("application/json")
                    .build();

            InvokeModelResponse response = bedrockRuntimeClient.invokeModel(request);

            String responseBody = response.body().asUtf8String();

            System.out.println(responseBody);

            JsonNode jsonResponse = objectMapper.readTree(responseBody);

            String analysisText = jsonResponse
                    .get("content")
                    .get(0)
                    .get("text")
                    .asText();

            if (analysisText.startsWith("```")) {
                analysisText = analysisText.replaceAll("```json?|```", "").trim();
            }

            // JSON 파싱
            JsonNode analysisJson = objectMapper.readTree(analysisText);

            // 강점 추출
            List<String> strengths = new ArrayList<>();
            analysisJson.get("strengths").forEach(node -> strengths.add(node.asText()));

            // 약점 추출
            List<String> weaknesses = new ArrayList<>();
            analysisJson.get("weaknesses").forEach(node -> weaknesses.add(node.asText()));

            // AI 피드백
            String aiFeedback = analysisJson.get("aiFeedback").asText();

            // 역량별 점수 추출
            Map<Competency, Integer> competencyScores = new HashMap<>();
            JsonNode scoresNode = analysisJson.get("competencyScores");

            competencyScores.put(Competency.PROBLEM_SOLVING, scoresNode.get("PROBLEM_SOLVING").asInt());
            competencyScores.put(Competency.COMMUNICATION, scoresNode.get("COMMUNICATION").asInt());
            competencyScores.put(Competency.TEAMWORK, scoresNode.get("TEAMWORK").asInt());
            competencyScores.put(Competency.ADAPTABILITY, scoresNode.get("ADAPTABILITY").asInt());
            competencyScores.put(Competency.INITIATIVE, scoresNode.get("INITIATIVE").asInt());

            return InterviewAnalysisResult.builder()
                    .strengths(strengths)
                    .weaknesses(weaknesses)
                    .aiFeedback(aiFeedback)
                    .competencyScores(competencyScores)
                    .build();

        } catch (Exception e) {
            log.error("면접 분석 실패: {}", e.getMessage(), e);
            throw InterviewExceptions.Parsing_Failed.toException();
        }
    }
}

