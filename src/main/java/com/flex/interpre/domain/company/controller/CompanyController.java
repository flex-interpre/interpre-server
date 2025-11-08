package com.flex.interpre.domain.company.controller;

import com.flex.interpre.domain.company.dto.CompanyDetailResponse;
import com.flex.interpre.domain.company.entity.Company;
import com.flex.interpre.domain.company.service.CompanyService;
import com.flex.interpre.domain.company.dto.CompanySummaryResponse;
import com.flex.interpre.domain.user.dto.request.UpdateMyCompanyInfo;
import com.flex.interpre.domain.user.dto.response.MyCompanyInfo;
import com.flex.interpre.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import java.util.UUID;

@RestController
@RequestMapping("/companies")
@RequiredArgsConstructor
public class CompanyController {
    private final CompanyService companyService;

    @GetMapping("/me")
    @Operation(summary = "내 기업 정보 조회")
    @PreAuthorize("hasRole('COMPANY')")
    public ApiResponse<MyCompanyInfo> getMyInfo(@AuthenticationPrincipal @Parameter(hidden = true) Company company) {
        return ApiResponse.ok(MyCompanyInfo.from(company));
    }

    @PutMapping("/me")
    @Operation(summary = "내 기업 정보 수정")
    @PreAuthorize("hasRole('COMPANY')")
    public ApiResponse<MyCompanyInfo> updateMyInfo(@AuthenticationPrincipal @Parameter(hidden = true) Company company,
                                                   @Valid @RequestBody UpdateMyCompanyInfo request) {
        return ApiResponse.ok(companyService.updateMyInfo(company, request));
    }

    @Operation(summary = "회사 목록 조회")
    @GetMapping
    public ApiResponse<Page<CompanySummaryResponse>> getCompanies(@PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(companyService.getCompanies(pageable));
    }

    @Operation(summary = "회사 상세 조회")
    @GetMapping("/{companyId}")
    public ApiResponse<CompanyDetailResponse> getCompanyDetails(@PathVariable UUID companyId) {
        return ApiResponse.ok(companyService.getCompanyDetails(companyId));
    }
}
