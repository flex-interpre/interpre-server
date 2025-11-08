package com.flex.interpre.domain.user.dto.response;

import com.flex.interpre.domain.company.entity.Company;
import jakarta.annotation.Nonnull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record MyCompanyInfo(
        UUID id,
        String email,
        String companyName,
        String businessNumber,
        String address,
        String website,
        String description,
        String logoUrl
) implements MyUserDetailInfo {
    public static MyCompanyInfo from(@Nonnull Company company) {
        return MyCompanyInfo.builder()
                .id(company.getId())
                .email(company.getEmail())
                .companyName(company.getCompanyName())
                .businessNumber(company.getBusinessNumber())
                .address(company.getAddress())
                .website(company.getWebsite())
                .description(company.getDescription())
                .logoUrl(company.getLogoUrl())
                .build();
    }
}
