package com.flex.interpre.domain.user.dto.response;

import com.flex.interpre.domain.user.entity.Company;
import lombok.Builder;

@Builder
public record MyCompanyInfo(
        String googleId,
        String email,
        String companyName,
        String businessNumber,
        String address,
        String website,
        String description,
        String logoUrl
) implements MyUserDetailInfo {
    public static MyCompanyInfo from(Company company) {
        if  (company == null) {
            return null;
        }
        return MyCompanyInfo.builder()
                .googleId(company.getUser().getGoogleId())
                .email(company.getUser().getEmail())
                .companyName(company.getCompanyName())
                .businessNumber(company.getBusinessNumber())
                .address(company.getAddress())
                .website(company.getWebsite())
                .description(company.getDescription())
                .logoUrl(company.getLogoUrl())
                .build();
    }
}
