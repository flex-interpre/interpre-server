package com.flex.interpre.domain.auth.controller;

import com.flex.interpre.domain.auth.dto.request.RefreshTokenRequest;
import com.flex.interpre.domain.auth.dto.response.TokenResponse;
import com.flex.interpre.domain.auth.service.AuthService;
import com.flex.interpre.global.dto.ApiResponse;
import com.flex.interpre.global.security.authentication.AccountPrincipal;
import io.swagger.v3.oas.annotations.Parameter;
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
    public ApiResponse<Void> logout(@AuthenticationPrincipal @Parameter(hidden = true) AccountPrincipal principal,
                                    @Valid @RequestBody RefreshTokenRequest request) {

        authService.logout(principal.getId(), request.getRefreshToken());

        return ApiResponse.ok();
    }

    @PatchMapping("/sessions")
    public ApiResponse<TokenResponse> regenerateToken(@RequestHeader("Authorization") String header,
                                                      @Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {

        String accessToken = header.replace("Bearer ", "");
        return ApiResponse.ok(authService.regenerateToken(accessToken, refreshTokenRequest.getRefreshToken()));
    }
}
