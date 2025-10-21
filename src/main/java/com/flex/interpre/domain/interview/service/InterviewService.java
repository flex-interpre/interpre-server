package com.flex.interpre.domain.interview.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flex.interpre.domain.interview.dto.response.ClovaSttResponse;
import com.flex.interpre.domain.interview.dto.response.SessionResponse;
import com.flex.interpre.domain.interview.entity.Interview;
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
import org.springframework.web.reactive.function.client.WebClient;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
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

    public String generateStartQuestions(String document) {

        String content = """
                    당신은 경험이 풍부한 면접관입니다.
                
                    지원자의 자소서:
                    ---
                    %s
                    ---
                
                    면접을 시작합니다. 지원자에게 간단한 인사말과 함께 자소서 내용을 바탕으로 한 첫 질문을 해주세요.
                
                    예시 형식:
                    "안녕하세요 홍길동님, 오늘 면접에 참여해 주셔서 감사합니다. 자소서에서 언급하신 프로젝트 경험에 대해 먼저 여쭤보고 싶은데요, ..."
                
                    위와 같이 인사 + 질문 1개만 작성해주세요.
                """.formatted(document);

        Map<String, Object> payloadMap = Map.of(
                "anthropic_version", "bedrock-2023-05-31",
                "max_tokens", 800,
                "temperature", 0.7,
                "top_p", 0.9,
                "top_k", 50,
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", content
                        )
                )
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
}

