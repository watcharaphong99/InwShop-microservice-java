package com.inwza.inventory.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "inventory_event_cancels")
public class InventoryEventCancelEntity {

    @Id
    @Column(name = "event_id", length = 128)
    private String eventId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public static InventoryEventCancelEntity of(String eventId) {
        InventoryEventCancelEntity entity = new InventoryEventCancelEntity();
        entity.eventId = eventId;
        return entity;
    }

    public String getEventId() {
        return eventId;
    }
}
