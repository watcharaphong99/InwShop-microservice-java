package com.inwza.player.infrastructure.persistence;

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

    @Column(nullable = false, length = 64)
    private String topic;

    @Column(name = "partition_id", nullable = false)
    private int partitionId;

    @Column(name = "offset_value", nullable = false)
    private long offsetValue;

    public String getId() {
        return id;
    }

    public long getOffsetValue() {
        return offsetValue;
    }

    public void setOffsetValue(long offsetValue) {
        this.offsetValue = offsetValue;
    }
}
