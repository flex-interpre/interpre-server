package com.flex.interpre.global.config;

import com.flex.interpre.global.property.BedrockProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

@Configuration
@RequiredArgsConstructor
public class BedrockConfig {

    private final BedrockProperty bedrockProperty;

    @Bean
    public BedrockRuntimeClient bedrockRuntimeClient() {
        return BedrockRuntimeClient.builder()
                .region(Region.of(bedrockProperty.getRegion()))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        bedrockProperty.getAccessKey(),
                                        bedrockProperty.getSecretKey()
                                )
                        )
                )
                .build();
    }
}
