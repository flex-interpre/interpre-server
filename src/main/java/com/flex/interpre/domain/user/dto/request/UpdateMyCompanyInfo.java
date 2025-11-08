package com.flex.interpre.domain.user.dto.request;


public record UpdateMyCompanyInfo(
        String companyName,
        String address,
        String website,
        String description,
        String logoUrl
){
}
