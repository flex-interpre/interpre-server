package com.flex.interpre.domain.recruitment.exception;

import com.flex.interpre.global.exception.ApiExceptions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RecruitmentExceptions implements ApiExceptions {

    INVALID_ROLE(HttpStatus.FORBIDDEN, "권한이 없습니다. (기업만 공고 등록 가능)"),
    COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND, "기업 정보를 찾을 수 없습니다."),
    RECRUITMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "공고를 찾을 수 없습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "본인 기업의 공고만 수정/삭제할 수 있습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
