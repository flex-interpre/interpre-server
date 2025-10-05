package com.flex.interpre.domain.recruitment.dto.response;

import com.flex.interpre.domain.user.entity.Company;
import java.util.UUID;

public record CompanySummaryResponse(
        UUID id,
        String name,
        String logoUrl
){
    public static CompanySummaryResponse from(Company company) {
        return new CompanySummaryResponse(
                company.getId(),
                company.getCompanyName(),
                company.getLogoUrl()
        );
    }
}