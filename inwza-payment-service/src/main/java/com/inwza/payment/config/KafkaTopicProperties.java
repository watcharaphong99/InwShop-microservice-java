package com.inwza.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "inwza.kafka")
public record KafkaTopicProperties(String playerTopic, String inventoryTopic, String paymentTopic) {
}
