package com.inwza.inventory.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "inwza.kafka")
public record KafkaTopicProperties(String inventoryTopic, String paymentTopic) {
}
