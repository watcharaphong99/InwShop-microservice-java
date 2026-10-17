package com.inwza.payment.application.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inwza.payment.application.PaymentDomainException;
import com.inwza.payment.config.KafkaTopicProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentKafkaPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaTopicProperties topics;
    private final ObjectMapper objectMapper;

    public PaymentKafkaPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            KafkaTopicProperties topics,
            ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.topics = topics;
        this.objectMapper = objectMapper;
    }

    public void capturePayment(PlayerTransactionMessage message) {
        publish(topics.playerTopic(), "buy", message, "error: capture payment failed");
    }

    public void addPlayerMoney(PlayerTransactionMessage message) {
        publish(topics.playerTopic(), "sell", message, "error: add player money failed");
    }

    public void rollbackTransaction(RollbackTransactionMessage message) {
        publish(topics.playerTopic(), "rtransaction", message, "error: rollback player transaction failed");
    }

    public void addPlayerItem(UpdateInventoryMessage message) {
        publish(topics.inventoryTopic(), "buy", message, "error: add player item failed");
    }

    public void removePlayerItem(UpdateInventoryMessage message) {
        publish(topics.inventoryTopic(), "sell", message, "error: remove player item failed");
    }

    public void rollbackAddPlayerItem(RollbackInventoryMessage message) {
        publish(topics.inventoryTopic(), "radd", message, "error: rollback add player item failed");
    }

    public void rollbackRemovePlayerItem(RollbackInventoryMessage message) {
        publish(topics.inventoryTopic(), "rremove", message, "error: rollback remove player item failed");
    }

    private void publish(String topic, String key, Object payload, String errorMessage) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(topic, key, json);
        } catch (JsonProcessingException ex) {
            throw new PaymentDomainException(errorMessage);
        }
    }
}
