package com.inwza.player.application.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inwza.player.application.PlayerApplicationService;
import com.inwza.player.infrastructure.persistence.KafkaConsumerOffsetEntity;
import com.inwza.player.infrastructure.persistence.KafkaConsumerOffsetJpaRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PlayerKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(PlayerKafkaListener.class);

    private final ObjectMapper objectMapper;
    private final PlayerApplicationService playerService;
    private final KafkaConsumerOffsetJpaRepository offsetRepository;

    public PlayerKafkaListener(
            ObjectMapper objectMapper,
            PlayerApplicationService playerService,
            KafkaConsumerOffsetJpaRepository offsetRepository) {
        this.objectMapper = objectMapper;
        this.playerService = playerService;
        this.offsetRepository = offsetRepository;
    }

    @KafkaListener(topics = "${inwza.kafka.player-topic}", groupId = "inwza-player-service")
    @Transactional
    public void onMessage(ConsumerRecord<String, String> record, Acknowledgment ack) {
        String key = record.key() == null ? "" : record.key();
        try {
            switch (key) {
                case "buy", "sell" -> {
                    PlayerTransactionMessage req =
                            objectMapper.readValue(record.value(), PlayerTransactionMessage.class);
                    if ("buy".equals(key)) {
                        playerService.dockedPlayerMoneyRes(req);
                    } else {
                        playerService.addPlayerMoneyRes(req);
                    }
                }
                case "rtransaction" -> {
                    RollbackTransactionMessage req =
                            objectMapper.readValue(record.value(), RollbackTransactionMessage.class);
                    playerService.rollbackPlayerTransaction(req);
                }
                default -> log.info("player skip key {} offset {}", key, record.offset());
            }
            offsetRepository.upsertMaxOffset(KafkaConsumerOffsetEntity.KAFKA_OFFSET_ID, record.offset() + 1);
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("player consumer offset {} key {}: {}", record.offset(), key, ex.getMessage());
        }
    }
}
