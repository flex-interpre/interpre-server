package com.flex.interpre.global.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String accessToken = jwtUtil.extractToken(request);

        try{
            if (jwtUtil.validateToken(accessToken)){
                UUID id = jwtUtil.extractUUID(accessToken);

                //TODO: id로 repo에서 User 객체 가져와서 authentication 생성
            }
        }catch (Exception e){

            // TODO: ApiException 처리 코드 생성
            return;
        }

        filterChain.doFilter(request,response);
    }
}
