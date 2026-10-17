package com.inwza.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "inwza.payment")
public record PaymentSagaProperties(
        long stepTimeoutMs,
        long workflowStaleSeconds,
        long workflowRecoverIntervalMs
) {
}
