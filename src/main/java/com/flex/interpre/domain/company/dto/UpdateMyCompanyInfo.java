package com.flex.interpre.domain.company.dto;


public record UpdateMyCompanyInfo(
        String companyName,
        String address,
        String website,
        String description,
        String logoUrl
){
}
