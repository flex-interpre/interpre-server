package com.flex.interpre.domain.company.dto;

import com.flex.interpre.domain.company.entity.Company;
import lombok.Builder;

import java.util.UUID;

@Builder
public record CompanySummaryResponse(
        UUID id,
        String name,
        String logoUrl
){
    public static CompanySummaryResponse from(Company company) {
        return CompanySummaryResponse.builder()
                .id(company.getUser().getId())
                .name(company.getCompanyName())
                .logoUrl(company.getLogoUrl())
                .build();
    }
}