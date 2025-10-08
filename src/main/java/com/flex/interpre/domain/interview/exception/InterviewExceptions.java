package com.flex.interpre.domain.interview.exception;

import com.flex.interpre.global.exception.ApiExceptions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum InterviewExceptions implements ApiExceptions {

    INVALID_SESSION_ID("잘못된 세션 아이디 입니다", HttpStatus.NOT_FOUND);

    private final String message;
    private final HttpStatus httpStatus;
}
