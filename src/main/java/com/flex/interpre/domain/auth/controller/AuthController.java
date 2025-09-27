package com.flex.interpre.domain.auth.controller;

import com.flex.interpre.domain.auth.dto.request.RefreshTokenRequest;
import com.flex.interpre.domain.auth.service.AuthService;
import com.flex.interpre.domain.user.entity.User;
import com.flex.interpre.global.dto.ApiResponse;
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
    public ApiResponse<?> logout(@AuthenticationPrincipal User user, @Valid @RequestBody RefreshTokenRequest request){

        authService.logout(user,request.getRefreshToken());

        return ApiResponse.ok();
    }
}
