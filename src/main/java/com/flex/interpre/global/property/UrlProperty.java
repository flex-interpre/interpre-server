package com.flex.interpre.global.property;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
@Data
@ConfigurationProperties(prefix = "app.oauth2")
public class UrlProperty {

    @NotBlank
    private Set<String> urls;
}
