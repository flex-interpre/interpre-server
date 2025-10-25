package com.flex.interpre.domain.interview.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flex.interpre.domain.document.entity.Document;
import com.flex.interpre.domain.interview.dto.response.ClovaSttResponse;
import com.flex.interpre.domain.interview.dto.response.InterviewDetailResponse;
import com.flex.interpre.domain.interview.dto.response.InterviewHistory;
import com.flex.interpre.domain.interview.dto.response.SessionResponse;
import com.flex.interpre.domain.interview.entity.Interview;
import com.flex.interpre.domain.interview.entity.InterviewChat;
import com.flex.interpre.domain.interview.entity.InterviewSession;
import com.flex.interpre.domain.interview.exception.InterviewExceptions;
import com.flex.interpre.domain.interview.repository.InterviewRepository;
import com.flex.interpre.domain.interview.repository.InterviewSessionRepository;
import com.flex.interpre.domain.user.entity.User;
import com.flex.interpre.global.property.BedrockProperty;
import com.flex.interpre.global.property.ClovaProperty;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewSessionRepository interviewSessionRepository;
    private final InterviewRepository interviewRepository;
    private final ClovaProperty clovaProperty;
    private final WebClient webClient;
    private final BedrockRuntimeClient bedrockRuntimeClient;
    private final BedrockProperty bedrockProperty;
    private final ObjectMapper objectMapper;


    @Transactional
    public SessionResponse getSessionResponse(User user, Document document) {

        Interview interview = interviewRepository.save(Interview.builder()
                .jobSeeker(user.getJobSeeker())
                .build());

        InterviewSession interviewSession = interviewSessionRepository.save(InterviewSession.builder()
                .userId(user.getId())
                .interviewId(interview.getId())
                .contentText(document.getContentText())
                .ttl(1)
                .build());
        return new SessionResponse(interviewSession.getId());
    }

    public String transcribe(byte[] audioData) {

        try {
            ClovaSttResponse response = webClient.post()
                    .uri(clovaProperty.getSttUrl() + "?lang=Kor")
                    .header("X-NCP-APIGW-API-KEY-ID", clovaProperty.getId())
                    .header("X-NCP-APIGW-API-KEY", clovaProperty.getSecret())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .bodyValue(audioData)
                    .retrieve()
                    .bodyToMono(ClovaSttResponse.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            if (response == null || response.text() == null || response.text().isEmpty()) {
                throw InterviewExceptions.STT_NO_RESULT.toException();
            }
            return response.text();

        } catch (Exception e) {
            throw InterviewExceptions.STT_PROCESSING_FAILED.toException();
        }
    }

    public String generateQuestions(String document, List<InterviewChat> chatHistory) {

        String systemPrompt = """
                당신은 기술 면접관입니다.
                
                면접 규칙:
                1. 자소서 내용을 바탕으로 질문하되, 대화 맥락을 고려하세요
                2. 답변 수준에 맞춰 질문 난이도를 조절하세요
                3. 반드시 한 번에 하나의 질문만 작성하세요. 절대로 두 개 이상의 질문을 한 응답에 포함하지 마세요.
                4. 답변이 불충분하면 후속 질문으로 깊이 파고드세요
                5. 적절한 때 다른 주제로 자연스럽게 전환하세요
                6. 사용자의 대답을 다음 질문에서 그대로 따라 하는 건 지양하고 비슷한 질문은 최대한 피하세요
                
                출력: 질문만 작성하고 다른 설명은 생략하세요.
                """;

        List<Map<String, Object>> messages = new ArrayList<>();

        // 인터뷰 기록이 없는 경우 첫 질문
        if (chatHistory.isEmpty()) {
            messages.add(Map.of(
                    "role", "user",
                    "content", """
                            면접을 시작합니다. 다음 자기소개서를 읽고 첫 질문을 해주세요.
                            
                            [자기소개서]
                            %s
                            
                            첫 질문은 자기소개를 요청하는 질문으로 시작하세요.
                            """.formatted(document)
            ));
        } else {
            // 히스토리 추가
            for (InterviewChat chat : chatHistory) {
                messages.add(Map.of("role", "assistant", "content", chat.getQuestion()));

                if (chat.getAnswer() != null && !chat.getAnswer().isBlank()) {
                    messages.add(Map.of("role", "user", "content", chat.getAnswer()));
                }
            }

            messages.add(Map.of(
                    "role", "user",
                    "content", """
                            이전 대화를 바탕으로 다음 질문을 해주세요.
                            
                            [자기소개서]
                            %s
                            """.formatted(document)
            ));
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
            byte[] audioData = webClient.post()
                    .uri(clovaProperty.getTtsUrl())
                    .header("X-NCP-APIGW-API-KEY-ID", clovaProperty.getId())
                    .header("X-NCP-APIGW-API-KEY", clovaProperty.getSecret())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData("speaker", "nara")
                            .with("text", text)
                            .with("volume", "0")
                            .with("speed", "-1")
                            .with("pitch", "1")
                            .with("emotion", "2")
                            .with("emotion-strength", "1")
                            .with("format", "wav")
                            .with("sampling-rate", "8000")
                            .with("alpha", "0")
                            .with("end-pitch", "0"))
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            return audioData;
        } catch (Exception e) {
            throw InterviewExceptions.TTS_PROCESSING_FAILED.toException();
        }
    }

    public List<InterviewHistory> getInterviewHistories(User user) {

        List<Interview> interviews = user.getJobSeeker().getInterviews();
        return interviews.stream().map(InterviewHistory::from).toList();
    }

    public InterviewDetailResponse getInterviewHistoryDetail(Interview interview) {

        return InterviewDetailResponse.builder()
                .id(interview.getId())
                .createdAt(interview.getCreatedAt())
                .title(interview.getTitle())
                .durationSeconds(interview.getDurationSecond())
                .qna(interview.getQnas())
                .build();
    }

    public InterviewDetailResponse updateInterviewTitle(Interview interview, String title) {

        interview.setTitle(title);
        Interview updatedInterview = interviewRepository.save(interview);

        return InterviewDetailResponse.builder()
                .id(updatedInterview.getId())
                .createdAt(updatedInterview.getCreatedAt())
                .title(updatedInterview.getTitle())
                .durationSeconds(updatedInterview.getDurationSecond())
                .qna(updatedInterview.getQnas())
                .build();
    }
}

