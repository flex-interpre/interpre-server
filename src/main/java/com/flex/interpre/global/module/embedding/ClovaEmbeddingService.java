package com.flex.interpre.global.module.embedding;

import com.flex.interpre.global.module.embedding.exception.ClovaEmbeddingExceptions;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClovaEmbeddingService {

    private static final String SUCCESS_CODE = "20000";
    private static final int EXPECTED_VECTOR_SIZE = 1024;

    private final RestTemplate restTemplate;

    @Value("${naver.clova.url}")
    private String apiUrl;

//    @Value("${naver.clova.api-key-id}")
//    private String apiKeyId;

    @Value("${naver.clova.api-key}")
    private String apiKey;

    @PostConstruct
    public void validate() {
        if (apiUrl == null || apiUrl.isBlank()) {
            throw new IllegalStateException("CLOVA API URL이 설정되지 않았습니다.");
        }
//        if (apiKeyId == null || apiKeyId.isBlank()) {
//            throw new IllegalStateException("CLOVA API Key ID가 설정되지 않았습니다.");
//        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("CLOVA API Key가 설정되지 않았습니다.");
        }
        log.info("ClovaEmbeddingService 초기화 완료");
    }

    // 임베딩 요청 DTO
    private record EmbeddingRequest(String text) {}

    // 응답 DTO
    private record EmbeddingResponse(Status status, Result result) {}
    private record Status(String code, String message) {}
    private record Result(List<Double> embedding, Integer totalTokens) {}

    public List<Double> embed(String text) {
        validateInput(text);

        HttpHeaders headers = createHeaders();
        EmbeddingRequest requestBody = new EmbeddingRequest(text);
        HttpEntity<EmbeddingRequest> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            log.debug("CLOVA 임베딩 API 호출: text length={}", text.length());

            EmbeddingResponse response = restTemplate.postForObject(
                    apiUrl,
                    requestEntity,
                    EmbeddingResponse.class
            );

            return extractEmbedding(response);

        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("CLOVA API 인증 실패: {}", e.getMessage());
            throw ClovaEmbeddingExceptions.AUTHENTICATION_FAILED.toException();

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("CLOVA API HTTP 오류 - status: {}, body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw ClovaEmbeddingExceptions.API_CALL_FAILED.toException();

        } catch (RestClientException e) {
            log.error("CLOVA API 네트워크 오류: {}", e.getMessage(), e);
            throw ClovaEmbeddingExceptions.API_CALL_FAILED.toException();

        } catch (Exception e) {
            log.error("CLOVA 임베딩 처리 중 예외 발생: {}", e.getMessage(), e);
            throw ClovaEmbeddingExceptions.API_CALL_FAILED.toException();
        }
    }

    private void validateInput(String text) {
        if (text == null || text.isBlank()) {
            throw ClovaEmbeddingExceptions.EMPTY_TEXT.toException();
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
//        headers.set("X-NCP-CLOVASTUDIO-REQUEST-ID", apiKeyId);
        headers.set("X-NCP-CLOVASTUDIO-REQUEST-ID", UUID.randomUUID().toString());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private List<Double> extractEmbedding(EmbeddingResponse response) {
        if (response == null) {
            log.error("API 응답이 null입니다.");
            throw ClovaEmbeddingExceptions.INVALID_RESPONSE.toException();
        }

        Status status = response.status();
        if (status == null || status.code() == null) {
            log.error("응답 상태 정보가 없습니다.");
            throw ClovaEmbeddingExceptions.INVALID_RESPONSE.toException();
        }

        if (!SUCCESS_CODE.equals(status.code())) {
            log.error("API 오류 - code: {}, message: {}", status.code(), status.message());
            throw ClovaEmbeddingExceptions.API_ERROR.toException();
        }

        Result result = response.result();
        if (result == null || result.embedding() == null || result.embedding().isEmpty()) {
            log.error("임베딩 결과가 없습니다.");
            throw ClovaEmbeddingExceptions.INVALID_RESPONSE.toException();
        }

        List<Double> embedding = result.embedding();

        // 벡터 크기 검증
        if (embedding.size() != EXPECTED_VECTOR_SIZE) {
            log.error("예상 벡터 크기: {}, 실제 크기: {}", EXPECTED_VECTOR_SIZE, embedding.size());
            throw ClovaEmbeddingExceptions.EMBEDDING_SIZE_MISMATCH.toException();
        }

        log.info("임베딩 성공: dimension={}, tokens={}",
                embedding.size(), result.totalTokens());

//        return toFloatArray(embedding);
        return embedding;
    }

    private float[] toFloatArray(List<Double> doubleList) {
        float[] floatArray = new float[doubleList.size()];
        for (int i = 0; i < doubleList.size(); i++) {
            floatArray[i] = doubleList.get(i).floatValue();
        }
        return floatArray;
    }
}