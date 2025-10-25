package com.flex.interpre.domain.recruitment.controller;


import com.flex.interpre.domain.recruitment.dto.request.RecruitmentCreateUpdateRequest;
import com.flex.interpre.domain.recruitment.dto.request.RecruitmentSearchRequest;
import com.flex.interpre.domain.recruitment.dto.response.RecruitmentResponse;
import com.flex.interpre.domain.recruitment.dto.response.RecruitmentSummaryResponse;
import com.flex.interpre.domain.recruitment.entity.Recruitment;
import com.flex.interpre.domain.recruitment.service.RecruitmentService;
import com.flex.interpre.domain.user.entity.User;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/recruitments")
@RequiredArgsConstructor
public class RecruitmentController {
    private final RecruitmentService recruitmentService;

    @Operation(summary = "공고문 생성")
    @PostMapping
    public ApiResponse<RecruitmentResponse> create(@AuthenticationPrincipal @Parameter(hidden = true) User user, @Valid @RequestBody RecruitmentCreateUpdateRequest request) {
        return ApiResponse.ok(recruitmentService.createRecruitment(user, request));
    }

    @Operation(summary = "공고문 전체 조회")
    @GetMapping
    public ApiResponse<Page<RecruitmentSummaryResponse>> getAllRecruitments(@PageableDefault(size = 30, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(recruitmentService.getAllRecruitments(pageable));
    }

    @Operation(summary = "공고문 목록 조회 (검색 + 필터 + 정렬)")
    @GetMapping("/search")
    public ApiResponse<Page<RecruitmentSummaryResponse>> searchRecruitments(@ParameterObject @Valid RecruitmentSearchRequest request) {
        return ApiResponse.ok(recruitmentService.searchRecruitments(request));
    }

    @Operation(summary = "공고문 상세 조회")
    @GetMapping("/{recruitment}")
    public ApiResponse<RecruitmentResponse> getRecruitment(@PathVariable Recruitment recruitment) {
        return ApiResponse.ok(recruitmentService.getRecruitment(recruitment));
    }

    @Operation(summary = "공고문 수정")
    @PutMapping("/{recruitment}")
    public ApiResponse<RecruitmentResponse> updateRecruitment(@PathVariable Recruitment recruitment, @Valid @RequestBody RecruitmentCreateUpdateRequest request) {
        return ApiResponse.ok(recruitmentService.updateRecruitment(recruitment, request));
    }

    @Operation(summary = "공고문 삭제")
    @DeleteMapping("/{recruitment}")
    public ApiResponse<Void> deleteRecruitment(@PathVariable Recruitment recruitment) {
        recruitmentService.deleteRecruitment(recruitment);
        return ApiResponse.ok();
    }
}
