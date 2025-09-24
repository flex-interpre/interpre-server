package com.flex.interpre.domain.auth.dto.response;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleUserInfo {

    @NotBlank
    String id;

    @NotBlank
    String email;

    @NotBlank
    String name;

}
