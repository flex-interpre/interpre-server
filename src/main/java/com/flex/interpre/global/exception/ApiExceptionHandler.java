package com.flex.interpre.global.exception;

import com.flex.interpre.global.dto.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.resource.NoResourceFoundException;

import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({ NoResourceFoundException.class, HttpRequestMethodNotSupportedException.class })
    public ApiResponse<?> noResourceFoundException(Exception ignored) {

        return ApiResponse.error(HttpStatus.NOT_FOUND, "요청하신 리소스를 찾을 수 없습니다.");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<?> httpMessageNotReadableException(HttpMessageNotReadableException ignored) {

        return ApiResponse.error(HttpStatus.BAD_REQUEST, "요청 데이터가 올바르지 않습니다.");
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ApiResponse<?> authorizationDeniedException(AuthorizationDeniedException ignored) {

        return ApiResponse.error(HttpStatus.UNAUTHORIZED, "권한이 없습니다.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<?> methodArgumentNotValidException(MethodArgumentNotValidException e) {

        if (Objects.isNull(e.getBindingResult().getFieldError())) {

            return ApiResponse.error(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        String field = e.getBindingResult().getFieldError().getField();
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        String errorMessage = String.format("%s은(는) %s", field, message);

        return ApiResponse.error(HttpStatus.BAD_GATEWAY, errorMessage);
    }

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
