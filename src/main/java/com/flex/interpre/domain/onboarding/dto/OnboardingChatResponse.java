package com.flex.interpre.domain.onboarding.dto;

import lombok.Builder;

// 온보딩 대화 응답
@Builder
public record OnboardingChatResponse(
        String aiResponse,
        String currentStep,
        Boolean isCompleted,
        OnboardingResult result
) {}
