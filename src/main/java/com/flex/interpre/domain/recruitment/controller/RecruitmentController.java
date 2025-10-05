package com.flex.interpre.domain.recruitment.controller;


import com.flex.interpre.domain.recruitment.dto.request.RecruitmentCreateUpdateRequest;
import com.flex.interpre.domain.recruitment.dto.response.RecruitmentResponse;
import com.flex.interpre.domain.recruitment.dto.response.RecruitmentSummaryResponse;
import com.flex.interpre.domain.recruitment.service.RecruitmentService;
import com.flex.interpre.domain.user.entity.User;
import com.flex.interpre.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/recruitments")
@RequiredArgsConstructor
public class RecruitmentController {
    private final RecruitmentService recruitmentService;

    @Operation(summary = "공고문 생성")
    @PostMapping
    public ApiResponse<RecruitmentResponse> create(@AuthenticationPrincipal User user, @Valid @RequestBody RecruitmentCreateUpdateRequest request) {
        return ApiResponse.ok(recruitmentService.createRecruitment(user, request));
    }

    @Operation(summary = "공고문 전체 조회")
    @GetMapping
    public ApiResponse<Page<RecruitmentSummaryResponse>> getAllRecruitments(@PageableDefault(size = 30, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(recruitmentService.getAllRecruitments(pageable));
    }

    @Operation(summary = "공고문 상세 조회")
    @GetMapping("/{recruitmentId}")
    public ApiResponse<RecruitmentResponse> getRecruitment(@PathVariable UUID recruitmentId) {
        return ApiResponse.ok(recruitmentService.getRecruitment(recruitmentId));
    }

    @Operation(summary = "공고문 수정")
    @PutMapping("/{recruitmentId}")
    public ApiResponse<RecruitmentResponse> updateRecruitment(@AuthenticationPrincipal User user, @PathVariable UUID recruitmentId, @Valid @RequestBody RecruitmentCreateUpdateRequest request) {
        return ApiResponse.ok(recruitmentService.updateRecruitment(user, recruitmentId, request));
    }

    @Operation(summary = "공고문 삭제")
    @DeleteMapping("/{recruitmentId}")
    public ApiResponse<Void> deleteRecruitment(@AuthenticationPrincipal User user, @PathVariable UUID recruitmentId) {
        recruitmentService.deleteRecruitment(user, recruitmentId);
        return ApiResponse.ok();
    }
}
