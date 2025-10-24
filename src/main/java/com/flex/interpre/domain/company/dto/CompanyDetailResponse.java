package com.flex.interpre.domain.company.dto;

import com.flex.interpre.domain.company.entity.Company;
import com.flex.interpre.domain.recruitment.dto.response.RecruitmentSummaryResponse;
import lombok.Builder;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Builder
public record CompanyDetailResponse(
        UUID id,
        String companyName,
        String businessNumber,
        String address,
        String website,
        String description,
        String logoUrl,

        List<RecruitmentSummaryResponse> recruitments
) {
    public static CompanyDetailResponse from(Company company) {
        List<RecruitmentSummaryResponse> recruitmentDtos = company.getRecruitments().stream()
                .filter(recruitment -> recruitment.isActive())
                .map(RecruitmentSummaryResponse::from)
                .collect(Collectors.toList());

        return CompanyDetailResponse.builder()
                .id(company.getUser().getId())
                .companyName(company.getCompanyName())
                .logoUrl(company.getLogoUrl())
                .description(company.getDescription())
                .address(company.getAddress())
                .website(company.getWebsite())
                .recruitments(recruitmentDtos)
                .build();
    }
}
