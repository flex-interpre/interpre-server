package com.flex.interpre.domain.interview.exception;

import com.flex.interpre.global.exception.ApiExceptions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum InterviewExceptions implements ApiExceptions {

    INVALID_SESSION_ID("잘못된 세션 아이디 입니다", HttpStatus.NOT_FOUND),
    NO_AUDIO_DATA("녹음된 데이터가 없습니다", HttpStatus.BAD_REQUEST),
    STT_PROCESSING_FAILED("음성 인식 처리 중 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    STT_NO_RESULT("음성 인식 결과가 없습니다", HttpStatus.BAD_REQUEST),
    Parsing_Failed("Claude 응답 파싱 실패", HttpStatus.BAD_REQUEST);

    private final String message;
    private final HttpStatus httpStatus;
}
