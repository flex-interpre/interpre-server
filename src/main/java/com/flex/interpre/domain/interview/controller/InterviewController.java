package com.flex.interpre.domain.interview.controller;

import com.flex.interpre.domain.interview.dto.request.ModifyTitleRequest;
import com.flex.interpre.domain.interview.dto.response.InterviewDetailResponse;
import com.flex.interpre.domain.interview.dto.response.InterviewHistory;
import com.flex.interpre.domain.interview.dto.response.SessionResponse;
import com.flex.interpre.domain.interview.entity.Interview;
import com.flex.interpre.domain.interview.service.InterviewService;
import com.flex.interpre.domain.jobSeeker.entity.JobSeeker;
import com.flex.interpre.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/interviews")
@PreAuthorize("hasRole('JOB_SEEKER')")
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping("/socket/{documentId}")
    @Operation(summary = "소켓 열기")
    public ApiResponse<SessionResponse> openSocket(@AuthenticationPrincipal @Parameter(hidden = true) JobSeeker jobSeeker,
                                                   @PathVariable UUID documentId) {
        return ApiResponse.ok(interviewService.getSessionResponse(jobSeeker, documentId));
    }

    @GetMapping
    @Operation(summary = "면접 기록 목록 조회")
    public ApiResponse<List<InterviewHistory>> getInterviewHistories(@AuthenticationPrincipal @Parameter(hidden = true) JobSeeker jobSeeker) {
        return ApiResponse.ok(interviewService.getInterviewHistories(jobSeeker));
    }

    @GetMapping("/{interview}")
    @Operation(summary = "면접 기록 상세 조회")
    public ApiResponse<InterviewDetailResponse> getInterviewHistoryDetail(@PathVariable Interview interview) {
        return ApiResponse.ok(interviewService.getInterviewHistoryDetail(interview));
    }

    @PatchMapping("/{interview}/name")
    @Operation(summary = "면접 기록 제목 수정")
    public ApiResponse<InterviewDetailResponse> updateInterviewTitle(@PathVariable Interview interview,
                                                                     @RequestBody ModifyTitleRequest request) {
        return ApiResponse.ok(interviewService.updateInterviewTitle(interview, request.title()));
    }
}
