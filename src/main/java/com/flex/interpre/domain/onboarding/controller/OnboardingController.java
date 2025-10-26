package com.flex.interpre.domain.onboarding.controller;

import com.flex.interpre.domain.onboarding.dto.OnboardingChatRequest;
import com.flex.interpre.domain.onboarding.dto.OnboardingChatResponse;
import com.flex.interpre.domain.onboarding.model.OnboardingSessionCache;
import com.flex.interpre.domain.onboarding.service.OnboardingAIService;
import com.flex.interpre.domain.user.entity.User;
import com.flex.interpre.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingAIService onboardingAIService;

    @Operation(description = "온보딩 채팅")
    @PostMapping("/chat")
    public ApiResponse<OnboardingChatResponse> chat(@AuthenticationPrincipal User user, @RequestBody OnboardingChatRequest request) {
        return ApiResponse.ok(onboardingAIService.chat(user, request));
    }


    @Operation(description = "채팅 히스토리 조회")
    @GetMapping("/history")
    public ApiResponse<List<OnboardingSessionCache.ChatMessage>> getChatHistory(@AuthenticationPrincipal User user) {
        return ApiResponse.ok(onboardingAIService.getChatHistory(user.getId()));
    }


    @Operation(description = "세션 초기화 (재시작)")
    @DeleteMapping("/session")
    public ApiResponse<Void> resetSession(@AuthenticationPrincipal User user) {
        onboardingAIService.resetSession(user.getId());
        return ApiResponse.ok();
    }
}