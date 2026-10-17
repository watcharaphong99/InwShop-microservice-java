package com.inwza.payment.application.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inwza.payment.application.PaymentSagaService;
import com.inwza.payment.infrastructure.persistence.KafkaConsumerOffsetEntity;
import com.inwza.payment.infrastructure.persistence.KafkaConsumerOffsetJpaRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentKafkaListener.class);

    private final ObjectMapper objectMapper;
    private final PaymentSagaService paymentSagaService;
    private final KafkaConsumerOffsetJpaRepository offsetRepository;

    public PaymentKafkaListener(
            ObjectMapper objectMapper,
            PaymentSagaService paymentSagaService,
            KafkaConsumerOffsetJpaRepository offsetRepository) {
        this.objectMapper = objectMapper;
        this.paymentSagaService = paymentSagaService;
        this.offsetRepository = offsetRepository;
    }

    @KafkaListener(topics = "${inwza.kafka.payment-topic}", groupId = "inwza-payment-service")
    @Transactional
    public void onPaymentReply(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            PaymentTransferMessage message = objectMapper.readValue(record.value(), PaymentTransferMessage.class);
            paymentSagaService.acceptPaymentReply(message);
            offsetRepository.upsertMaxOffset(KafkaConsumerOffsetEntity.KAFKA_OFFSET_ID, record.offset() + 1);
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("payment reply decode offset {}: {}", record.offset(), ex.getMessage());
        }
    }
}
