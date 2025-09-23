package com.flex.interpre.global.exception;

import com.flex.interpre.global.dto.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    // DTO 유효성 검증 실패 시 (@RequestBody @Valid 등 에서 감지)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<?> handleValidation(MethodArgumentNotValidException e) {
        FieldError error = e.getBindingResult().getFieldError();
//        String field = (error != null) ? error.getField() : "unknown";
        String message = (error != null && error.getDefaultMessage() != null)
                ? error.getDefaultMessage() : "잘못된 요청입니다.";
        return ApiResponse.error(HttpStatus.BAD_REQUEST, message);
    }

    // 파라미터 검증 실패 시
    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<?> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .findFirst()
                .orElse("잘못된 요청입니다.");
        return ApiResponse.error(HttpStatus.BAD_REQUEST, message);
    }

    // 권한 요청 실패 시
    @ExceptionHandler(AccessDeniedException.class)
    public ApiResponse<Void> AccessDeniedException(AccessDeniedException e){
        return ApiResponse.error(HttpStatus.FORBIDDEN,"권한이 없습니다.");
    }

    // 커스텀 예외
    @ExceptionHandler(ApiException.class)
    public ApiResponse<?> handleApiException(ApiException e) {
        return ApiResponse.error(e.getHttpStatus(), e.getMessage());
    }

    // 모든 예외 fallback
    @ExceptionHandler(Exception.class)
    public ApiResponse<?> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 오류가 발생했습니다.");
    }
}
