package com.flex.interpre.domain.jobSeeker.dto;

import jakarta.validation.constraints.NotNull;

public record RecommendationFeedbackRequest(
        @NotNull Integer score // +1, 0, -1
){}
