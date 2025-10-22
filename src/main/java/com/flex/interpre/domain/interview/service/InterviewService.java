package com.flex.interpre.domain.interview.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flex.interpre.domain.interview.dto.response.ClovaSttResponse;
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

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public SessionResponse getSessionResponse(User user) {

        Interview interview = interviewRepository.save(Interview.builder()
                .jobSeeker(user.getJobSeeker())
                .build());

        InterviewSession interviewSession = interviewSessionRepository.save(InterviewSession.builder()
                .userId(user.getId())
                .interviewId(interview.getId())
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
                당신은 경험이 풍부한 기술 면접관입니다.
                
                지원자의 자기소개서:
                ---
                %s
                ---
                
                면접 규칙:
                1. 자소서 내용을 바탕으로 질문하되, 대화 맥락을 고려하세요
                2. 지원자의 답변 수준에 맞춰 질문의 난이도를 조절하세요
                3. 한 번에 1개의 질문만 하세요
                4. 답변이 불충분하면 후속 질문으로 깊이 파고드세요
                5. 적절한 때에 다른 주제로 자연스럽게 전환하세요
                
                첫 질문일 경우:
                "안녕하세요 [이름]님, 오늘 면접에 참여해 주셔서 감사합니다. ..." 형식으로 인사와 함께 시작하세요.
                """.formatted(document);

        List<Map<String, Object>> messages = new ArrayList<>();

        // 인터뷰 기록이 없는 경우 첫 질문
        if (chatHistory.isEmpty()) {
            messages.add(Map.of(
                    "role", "user",
                    "content", "면접을 시작합니다. 첫 질문을 해주세요."
            ));
        } else {
            for (InterviewChat chat : chatHistory) {
                // llm 질문 추가
                messages.add(Map.of(
                        "role", "assistant",
                        "content", chat.getQuestion()
                ));

                // 유저 답변 추가
                if (chat.getAnswer() != null && !chat.getAnswer().isBlank()) {
                    messages.add(Map.of(
                            "role", "user",
                            "content", chat.getAnswer()
                    ));
                }
            }
        }

        Map<String, Object> payloadMap = Map.of(
                "anthropic_version", "bedrock-2023-05-31",
                "max_tokens", 800,
                "temperature", 0.7,
                "top_p", 0.9,
                "top_k", 50,
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
            System.out.println(e.getMessage());
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
}

