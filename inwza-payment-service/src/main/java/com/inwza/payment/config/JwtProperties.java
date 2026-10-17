package com.inwza.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "inwza.jwt")
public record JwtProperties(String accessSecretKey, String apiSecretKey) {
}
