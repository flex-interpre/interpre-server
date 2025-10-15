package com.flex.interpre.global.module.embedding.exception;

import com.flex.interpre.global.exception.ApiExceptions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ClovaEmbeddingExceptions implements ApiExceptions {
    API_CALL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "임베딩 API 호출에 실패했습니다."),
    INVALID_RESPONSE(HttpStatus.INTERNAL_SERVER_ERROR, "API 응답이 올바르지 않습니다."),
    EMPTY_TEXT(HttpStatus.BAD_REQUEST, "임베딩할 텍스트가 비어있습니다."),
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "API 인증에 실패했습니다."),
    API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "API에서 오류를 반환했습니다."),
    EMBEDDING_SIZE_MISMATCH(HttpStatus.INTERNAL_SERVER_ERROR, "임베딩 벡터 크기가 예상과 다릅니다.");

    private final HttpStatus httpStatus;
    private final String message;
}