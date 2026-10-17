package com.inwza.payment.infrastructure.persistence;

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

    @Column(nullable = false)
    private String topic;

    @Column(name = "partition_id", nullable = false)
    private int partitionId;

    @Column(name = "offset_value", nullable = false)
    private long offsetValue;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getPartitionId() {
        return partitionId;
    }

    public void setPartitionId(int partitionId) {
        this.partitionId = partitionId;
    }

    public long getOffsetValue() {
        return offsetValue;
    }

    public void setOffsetValue(long offsetValue) {
        this.offsetValue = offsetValue;
    }
}
