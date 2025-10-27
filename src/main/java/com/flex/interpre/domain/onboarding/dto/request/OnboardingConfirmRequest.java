package com.flex.interpre.domain.onboarding.dto.request;

import java.util.Set;

public record OnboardingConfirmRequest(
        Set<String> areas,
        Set<String> jobFirsts,
        Set<String> jobSeconds
) {}
