package com.flex.interpre.domain.jobSeeker.controller;


import com.flex.interpre.domain.jobSeeker.dto.RecommendationFeedbackRequest;
import com.flex.interpre.domain.jobSeeker.entity.JobSeeker;
import com.flex.interpre.domain.jobSeeker.service.RecommendationFeedbackService;
import com.flex.interpre.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/feedback")
@PreAuthorize("hasRole('JOB_SEEKER')")
public class RecommendationFeedbackController {
    private final RecommendationFeedbackService feedbackService;

    @PostMapping("/recruitments/{recruitmentId}")
    @Operation(summary = "추천 매칭 피드백 제출 (+1, 0, -1)")
    public ApiResponse<Void> submitFeedback(@AuthenticationPrincipal @Parameter(hidden = true) JobSeeker jobSeeker,
                                            @PathVariable UUID recruitmentId,
                                            @Valid @RequestBody RecommendationFeedbackRequest request) {
        feedbackService.submitFeedback(jobSeeker, recruitmentId, request);
        return ApiResponse.ok();
    }
}
