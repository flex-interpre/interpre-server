package com.flex.interpre.domain.onboarding.controller;

import com.flex.interpre.domain.onboarding.dto.request.OnboardingChatRequest;
import com.flex.interpre.domain.onboarding.dto.request.OnboardingChoiceRequest;
import com.flex.interpre.domain.onboarding.dto.request.OnboardingConfirmRequest;
import com.flex.interpre.domain.onboarding.dto.response.OnboardingChatResponse;
import com.flex.interpre.domain.onboarding.model.OnboardingSessionCache;
import com.flex.interpre.domain.onboarding.service.OnboardingAIService;
import com.flex.interpre.domain.jobSeeker.entity.User;
import com.flex.interpre.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/onboarding")
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
        return ApiResponse.ok(onboardingAIService.getChatHistory(user));
    }

    @Operation(description = "세션 초기화 (재시작)")
    @DeleteMapping("/session")
    public ApiResponse<Void> resetSession(@AuthenticationPrincipal User user) {
        onboardingAIService.resetSession(user);
        return ApiResponse.ok();
    }

    @Operation(description = "사용자가 속상 선택 버튼을 클릭 -> 선택 스택에 쌓고, 해당 사항을 프롬프트 주입하여 LLM 대화 유지")
    @PostMapping("/choice")
    public ApiResponse<OnboardingChatResponse> handleChoice(@AuthenticationPrincipal User user, @RequestBody OnboardingChoiceRequest request){
        return ApiResponse.ok(onboardingAIService.handleChoice(user, request));
    }
    @Operation(description = "선택 완료-> 구직자 정보 업데이트 (DB 반영)")
    @PostMapping("/confirm")
    public ApiResponse<Void> confirmSelection(@AuthenticationPrincipal User user, @RequestBody OnboardingConfirmRequest request) {
        onboardingAIService.confirmSelections(user, request);
        return ApiResponse.ok();
    }

}