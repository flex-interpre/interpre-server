package com.flex.interpre.domain.user.dto.request;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.flex.interpre.domain.user.entity.Role;

public record UpdateMyCompanyInfo(
        String companyName,
        String businessNumber,
        String address,
        String website,
        String description,
        String logoUrl
) implements UserUpdateRequest {

    @JsonIgnore
    public Role getRole() {
        return Role.COMPANY;
    }
}
