package com.flex.interpre.domain.company.controller;

import com.flex.interpre.domain.company.dto.CompanyDetailResponse;
import com.flex.interpre.domain.company.service.CompanyService;
import com.flex.interpre.domain.company.dto.CompanySummaryResponse;
import com.flex.interpre.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.UUID;

@RestController
@RequestMapping("/companies")
@RequiredArgsConstructor
public class CompanyController {
    private final CompanyService companyService;

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
