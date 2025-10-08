package com.flex.interpre.global.module.pdf.exception;

import com.flex.interpre.global.exception.ApiExceptions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PdfExtractorExceptions implements ApiExceptions {

    FILE_EXTRACT_FAILED(HttpStatus.BAD_REQUEST, "PDF 파일 텍스트 추출에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}

