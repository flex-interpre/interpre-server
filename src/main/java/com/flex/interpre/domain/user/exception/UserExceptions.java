package com.flex.interpre.domain.user.exception;

import com.flex.interpre.global.exception.ApiExceptions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserExceptions implements ApiExceptions {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND,"해당 유저를 찾을 수 없습니다"),
    INVALID_ROLE(HttpStatus.BAD_REQUEST, "잘못된 사용자 역할입니다");

    private final HttpStatus httpStatus;
    private final String message;

}
