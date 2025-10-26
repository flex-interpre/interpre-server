package com.flex.interpre.domain.onboarding.dto;

import java.util.UUID;

// 온보딩 대화 요청
public record OnboardingChatRequest(
        UUID userId,
        String message
) {}