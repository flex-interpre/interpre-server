package com.flex.interpre.global.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flex.interpre.domain.user.entity.User;
import com.flex.interpre.domain.user.exception.UserExceptions;
import com.flex.interpre.domain.user.repository.UserRepository;
import com.flex.interpre.global.dto.ApiResponse;
import com.flex.interpre.global.exception.ApiException;
import com.flex.interpre.global.security.authentication.UserAuthentication;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String accessToken = jwtUtil.extractToken(request);

        try{
            if (jwtUtil.validateToken(accessToken)){
                UUID id = jwtUtil.extractUUID(accessToken);

                User user = userRepository.findById(id).orElseThrow(UserExceptions.USER_NOT_FOUND::toException);

                UserAuthentication authentication = new UserAuthentication(user);
                authentication.setAuthenticated(true);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }catch (ApiException e){

            ApiResponse<?> apiResponse = ApiResponse.error(e);

            String body = objectMapper.writeValueAsString(apiResponse);
            response.setStatus(e.getHttpStatus().value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(body);
            return;
        }

        filterChain.doFilter(request,response);
    }
}
