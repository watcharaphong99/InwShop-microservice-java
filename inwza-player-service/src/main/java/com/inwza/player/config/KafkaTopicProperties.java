package com.inwza.player.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "inwza.kafka")
public record KafkaTopicProperties(String playerTopic, String paymentTopic) {
}
