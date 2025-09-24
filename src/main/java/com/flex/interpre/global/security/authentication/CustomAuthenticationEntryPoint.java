package com.flex.interpre.global.security.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flex.interpre.domain.auth.exception.AuthExceptions;
import com.flex.interpre.global.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {

        ApiResponse<?> apiResponse = ApiResponse.error(AuthExceptions.AUTHENTICATION_FAILED.toException());

        String body = objectMapper.writeValueAsString(apiResponse);

        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(body);

    }
}
