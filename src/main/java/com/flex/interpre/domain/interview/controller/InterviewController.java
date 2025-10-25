package com.flex.interpre.domain.interview.controller;

import com.flex.interpre.domain.document.entity.Document;
import com.flex.interpre.domain.interview.dto.response.InterviewDetailResponse;
import com.flex.interpre.domain.interview.dto.response.InterviewHistory;
import com.flex.interpre.domain.interview.dto.response.SessionResponse;
import com.flex.interpre.domain.interview.entity.Interview;
import com.flex.interpre.domain.interview.service.InterviewService;
import com.flex.interpre.domain.user.entity.User;
import com.flex.interpre.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/interviews")
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping("/socket/{document}")
    @Operation(summary = "소켓 열기")
    public ApiResponse<SessionResponse> openSocket(@AuthenticationPrincipal User user,
                                                   @PathVariable Document document) {

        return ApiResponse.ok(interviewService.getSessionResponse(user, document));
    }

    @GetMapping
    @Operation(summary = "면접 기록 목록 조회")
    public ApiResponse<List<InterviewHistory>> getInterviewHistories(@AuthenticationPrincipal User user) {

        return ApiResponse.ok(interviewService.getInterviewHistories(user));
    }

    @GetMapping("/{interview}")
    @Operation(summary = "면접 기록 상세 조회")
    public ApiResponse<InterviewDetailResponse> getInterviewHistoryDetail(@PathVariable Interview interview) {

        return ApiResponse.ok(interviewService.getInterviewHistoryDetail(interview));
    }
}
