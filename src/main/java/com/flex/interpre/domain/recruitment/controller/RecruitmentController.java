package com.flex.interpre.domain.recruitment.controller;

import com.flex.interpre.domain.company.entity.Company;
import com.flex.interpre.domain.jobSeeker.entity.JobSeeker;
import com.flex.interpre.domain.recruitment.dto.request.RecruitmentCreateUpdateRequest;
import com.flex.interpre.domain.recruitment.dto.request.RecruitmentSearchRequest;
import com.flex.interpre.domain.recruitment.dto.response.RecruitmentResponse;
import com.flex.interpre.domain.recruitment.dto.response.RecruitmentSummaryResponse;
import com.flex.interpre.domain.recruitment.service.RecruitmentService;
import com.flex.interpre.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('COMPANY')")
    public ApiResponse<RecruitmentResponse> create(@AuthenticationPrincipal @Parameter(hidden = true) Company company,
                                                   @Valid @RequestBody RecruitmentCreateUpdateRequest request) {
        return ApiResponse.ok(recruitmentService.createRecruitment(company, request));
    }
    
    @Operation(summary = "공고문 전체 조회")
    @GetMapping
    public ApiResponse<Page<RecruitmentSummaryResponse>> getAllRecruitments(
            @PageableDefault(size = 30, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(recruitmentService.getAllRecruitments(pageable));
    }
    
    @Operation(summary = "공고문 추천 조회 (인터뷰 기반)")
    @GetMapping("/recommended")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ApiResponse<Page<RecruitmentSummaryResponse>> getRecommendedRecruitments(
            @AuthenticationPrincipal JobSeeker jobSeeker,
            @PageableDefault(size = 30) Pageable pageable) {
        return ApiResponse.ok(recruitmentService.getRecommendedRecruitments(jobSeeker, pageable));
    }
    
    @Operation(summary = "공고문 목록 조회 (검색 + 필터 + 정렬)")
    @GetMapping("/search")
    public ApiResponse<Page<RecruitmentSummaryResponse>> searchRecruitments(@ParameterObject @Valid RecruitmentSearchRequest request) {
        return ApiResponse.ok(recruitmentService.searchRecruitments(request));
    }
    
    @Operation(summary = "공고문 상세 조회")
    @GetMapping("/{recruitmentId}")
    public ApiResponse<RecruitmentResponse> getRecruitment(@PathVariable UUID recruitmentId) {
        return ApiResponse.ok(recruitmentService.getRecruitment(recruitmentId));
    }
    
    @Operation(summary = "공고문 수정")
    @PutMapping("/{recruitmentId}")
    @PreAuthorize("hasRole('COMPANY')")
    public ApiResponse<RecruitmentResponse> updateRecruitment(@AuthenticationPrincipal @Parameter(hidden = true) Company company,
                                                              @PathVariable UUID recruitmentId, @Valid @RequestBody RecruitmentCreateUpdateRequest request) {
        return ApiResponse.ok(recruitmentService.updateRecruitment(company, recruitmentId, request));
    }
    
    @Operation(summary = "공고문 삭제")
    @DeleteMapping("/{recruitmentId}")
    @PreAuthorize("hasRole('COMPANY')")
    public ApiResponse<Void> deleteRecruitment(@AuthenticationPrincipal @Parameter(hidden = true) Company company,
                                               @PathVariable UUID recruitmentId) {
        recruitmentService.deleteRecruitment(company, recruitmentId);
        return ApiResponse.ok();
    }
}