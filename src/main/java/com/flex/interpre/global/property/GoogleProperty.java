package com.flex.interpre.global.property;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.google")
public class GoogleProperty {

    @NotBlank
    private String clientId;

    @NotBlank
    private String redirectUrl;

    @NotBlank
    private String clientSecret;
}
