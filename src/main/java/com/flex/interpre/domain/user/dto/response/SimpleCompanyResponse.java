package com.flex.interpre.domain.user.dto.response;

import com.flex.interpre.domain.user.entity.Company;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor(staticName = "of")
@Schema(description = "간단 기업 응답")
public class SimpleCompanyResponse {

    private UUID id;

    private String name;

    private String logoUrl;

    public static SimpleCompanyResponse from(Company company) {

        return SimpleCompanyResponse.of(company.getId(), company.getCompanyName(), company.getLogoUrl());
    }
}
