package com.inwza.inventory.application.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inwza.inventory.application.InventoryApplicationService;
import com.inwza.inventory.infrastructure.persistence.KafkaConsumerOffsetEntity;
import com.inwza.inventory.infrastructure.persistence.KafkaConsumerOffsetJpaRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class InventoryKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(InventoryKafkaListener.class);

    private final ObjectMapper objectMapper;
    private final InventoryApplicationService inventoryService;
    private final KafkaConsumerOffsetJpaRepository offsetRepository;

    public InventoryKafkaListener(
            ObjectMapper objectMapper,
            InventoryApplicationService inventoryService,
            KafkaConsumerOffsetJpaRepository offsetRepository) {
        this.objectMapper = objectMapper;
        this.inventoryService = inventoryService;
        this.offsetRepository = offsetRepository;
    }

    @KafkaListener(topics = "${inwza.kafka.inventory-topic}", groupId = "inwza-inventory-service")
    @Transactional
    public void onMessage(ConsumerRecord<String, String> record, Acknowledgment ack) {
        String key = record.key() == null ? "" : record.key();
        try {
            switch (key) {
                case "buy" -> {
                    UpdateInventoryMessage req =
                            objectMapper.readValue(record.value(), UpdateInventoryMessage.class);
                    inventoryService.addPlayerItemRes(req);
                }
                case "sell" -> {
                    UpdateInventoryMessage req =
                            objectMapper.readValue(record.value(), UpdateInventoryMessage.class);
                    inventoryService.removePlayerItemRes(req);
                }
                case "radd" -> {
                    RollbackInventoryMessage req =
                            objectMapper.readValue(record.value(), RollbackInventoryMessage.class);
                    inventoryService.rollbackAdd(req);
                }
                case "rremove" -> {
                    RollbackInventoryMessage req =
                            objectMapper.readValue(record.value(), RollbackInventoryMessage.class);
                    inventoryService.rollbackRemove(req);
                }
                default -> log.info("inventory skip key {} offset {}", key, record.offset());
            }
            offsetRepository.upsertMaxOffset(KafkaConsumerOffsetEntity.KAFKA_OFFSET_ID, record.offset() + 1);
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("inventory consumer offset {} key {}: {}", record.offset(), key, ex.getMessage());
        }
    }
}
