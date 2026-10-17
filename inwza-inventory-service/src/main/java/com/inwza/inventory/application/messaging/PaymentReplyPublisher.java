package com.inwza.inventory.application.messaging;

import com.inwza.inventory.config.KafkaTopicProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentReplyPublisher {

    private final KafkaTemplate<String, PaymentTransferMessage> kafkaTemplate;
    private final KafkaTopicProperties topics;

    public PaymentReplyPublisher(
            KafkaTemplate<String, PaymentTransferMessage> kafkaTemplate, KafkaTopicProperties topics) {
        this.kafkaTemplate = kafkaTemplate;
        this.topics = topics;
    }

    public void publishBuyReply(PaymentTransferMessage message) {
        kafkaTemplate.send(topics.paymentTopic(), "buy", message);
    }

    public void publishSellReply(PaymentTransferMessage message) {
        kafkaTemplate.send(topics.paymentTopic(), "sell", message);
    }
}
