package com.inwza.inventory.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "kafka_consumer_offset")
public class KafkaConsumerOffsetEntity {

    public static final String KAFKA_OFFSET_ID = "kafka_offset";

    @Id
    private String id;

    @Column(name = "offset_value", nullable = false)
    private long offsetValue;

    public long getOffsetValue() {
        return offsetValue;
    }
}
