package com.flex.interpre.global.module.s3.exception;

import com.flex.interpre.global.exception.ApiExceptions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum S3UploaderExceptions implements ApiExceptions {

    DOCUMENT_NOT_FOUND(HttpStatus.BAD_REQUEST, "업로드할 파일이 없습니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "파일 크기가 최대 용량(10MB)을 초과했습니다."),
    UNSUPPORTED_FILE_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다. PDF 파일만 업로드 가능합니다."),
    INVALID_PDF_FILE(HttpStatus.BAD_REQUEST, "PDF 파일만 업로드 가능합니다."),
    INVALID_FILE_NAME(HttpStatus.BAD_REQUEST, "잘못된 파일명입니다."),
    FILE_EXTENSION_NOT_FOUND(HttpStatus.BAD_REQUEST, "파일 확장자를 찾을 수 없습니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
    INVALID_FILE_URL(HttpStatus.BAD_REQUEST, "유효하지 않은 파일 URL입니다."),
    FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "문서 삭제 중 오류가 발생했습니다.");


    private final HttpStatus httpStatus;
    private final String message;
}

