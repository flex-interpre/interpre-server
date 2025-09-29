package com.flex.interpre.domain.auth.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RefreshTokenRequest {

    @NotNull
    String refreshToken;
}
