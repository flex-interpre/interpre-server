package com.flex.interpre.domain.onboarding.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record OnboardingChoiceRequest(
        @NotBlank String type,   // AREA, JOB_FIRST, JOB_SECOND
        @NotBlank String value   // 실제 Enum name() 값
) {}
