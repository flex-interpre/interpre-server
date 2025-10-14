package com.flex.interpre.global.property;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Data
@Component
@ConfigurationProperties(prefix = "app.clova")
public class ClovaProperty {

    @NotBlank
    private String id;

    @NotBlank
    private String secret;

    @NotBlank
    private String url;
}
