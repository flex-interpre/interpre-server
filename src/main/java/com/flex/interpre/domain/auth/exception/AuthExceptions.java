package com.flex.interpre.domain.auth.exception;

import com.flex.interpre.global.exception.ApiExceptions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthExceptions implements ApiExceptions {

    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "엑세스 토큰이 만료 되었습니다."),
    AUTHENTICATION_FAILED(HttpStatus.BAD_REQUEST, "인증에 실패 했습니다"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "잘못된 인증 정보 입니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "리프레쉬 토큰이 만료되었습니다."),
    TOKEN_NOT_PAIR(HttpStatus.UNAUTHORIZED, "엑세스 토큰과 리프레쉬 토큰의 정보가 다릅니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
