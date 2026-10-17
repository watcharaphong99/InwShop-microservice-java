package com.inwza.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "inwza.jwt")
public record JwtProperties(
        String accessSecretKey,
        String refreshSecretKey,
        String apiSecretKey,
        long accessDurationSeconds,
        long refreshDurationSeconds
) {
}
