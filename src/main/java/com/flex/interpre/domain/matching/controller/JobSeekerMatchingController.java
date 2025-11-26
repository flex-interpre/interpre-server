package com.flex.interpre.domain.matching.controller;

import com.flex.interpre.domain.jobSeeker.entity.JobSeeker;
import com.flex.interpre.domain.matching.service.AIMatchingService;
import com.flex.interpre.domain.recruitment.dto.response.RecruitmentSummaryResponse;
import com.flex.interpre.domain.recruitment.entity.Recruitment;
import com.flex.interpre.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/jobseekers")
@PreAuthorize("hasRole('JOB_SEEKER')")
public class JobSeekerMatchingController {
    private final AIMatchingService aiMatchingService;

    @GetMapping("/recommendations")
    @Operation(summary = "구직자가 직접 추천 공고 요청")
    public ApiResponse<List<RecruitmentSummaryResponse>> getRecommendations(@AuthenticationPrincipal @Parameter(hidden = true) JobSeeker jobSeeker) {
        List<Recruitment> recommendations = aiMatchingService.recommend(jobSeeker);
        List<RecruitmentSummaryResponse> res = recommendations.stream().map(RecruitmentSummaryResponse::from).toList();

        return ApiResponse.ok(res);
    }
}
