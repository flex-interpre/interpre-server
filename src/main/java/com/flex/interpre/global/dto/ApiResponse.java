package com.flex.interpre.global.dto;

import org.springframework.http.HttpStatus;

public record ApiResponse<T>(
        int statusCode,
        String message,
        T data
){
    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(200, null, null);
    }

    public static <T> ApiResponse<T> ok(T content) {
        return new ApiResponse<>(200, null, content);
    }

    public static <T> ApiResponse<T> error(HttpStatus status, String error) {
        return new ApiResponse<>(status.value(), error, null);
    }
}
