package com.flex.interpre.domain.auth.controller;

import com.flex.interpre.domain.auth.dto.request.RefreshTokenRequest;
import com.flex.interpre.domain.auth.dto.response.TokenResponse;
import com.flex.interpre.domain.auth.service.AuthService;
import com.flex.interpre.domain.user.entity.User;
import com.flex.interpre.global.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @DeleteMapping("/sessions")
    public ApiResponse<Void> logout(@AuthenticationPrincipal User user, @Valid @RequestBody RefreshTokenRequest request) {

        authService.logout(user, request.getRefreshToken());

        return ApiResponse.ok();
    }

    @PatchMapping("/sessions")
    public ApiResponse<TokenResponse> regenerateToken(HttpServletRequest request, @Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {

        return ApiResponse.ok(authService.regenerateToken(request, refreshTokenRequest.getRefreshToken()));
    }
}
