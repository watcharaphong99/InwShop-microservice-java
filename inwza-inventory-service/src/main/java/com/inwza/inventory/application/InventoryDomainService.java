package com.inwza.inventory.application;

import com.inwza.inventory.infrastructure.persistence.InventoryEventCancelEntity;
import com.inwza.inventory.infrastructure.persistence.InventoryEventCancelJpaRepository;
import com.inwza.inventory.infrastructure.persistence.PlayerInventoryEntity;
import com.inwza.inventory.infrastructure.persistence.PlayerInventoryJpaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class InventoryDomainService {

    public static final String ERR_ITEM_NOT_FOUND = "error: item not found";
    public static final String ERR_EVENT_CANCELLED = "error: event cancelled";
    public static final String ERR_INSERT = "error: insert player item failed";
    public static final String ERR_REMOVE = "error: remove player item failed";

    private final PlayerInventoryJpaRepository inventory;
    private final InventoryEventCancelJpaRepository cancels;

    public InventoryDomainService(
            PlayerInventoryJpaRepository inventory, InventoryEventCancelJpaRepository cancels) {
        this.inventory = inventory;
        this.cancels = cancels;
    }

    @Transactional
    public UUID insertItem(String playerId, String itemId, String eventId) {
        String normalizedPlayer = PlayerIdCodec.format(playerId);
        if (eventId != null && !eventId.isBlank() && cancels.existsById(eventId)) {
            throw new InventoryDomainException(ERR_EVENT_CANCELLED);
        }

        if (eventId != null && !eventId.isBlank()) {
            var existing = inventory.findByEventId(eventId);
            if (existing.isPresent()) {
                return resolveExistingInsert(existing.get(), normalizedPlayer, itemId, eventId);
            }
        }

        PlayerInventoryEntity row = new PlayerInventoryEntity();
        row.setPlayerId(normalizedPlayer);
        row.setItemId(itemId);
        row.setEventId(eventId);
        try {
            inventory.save(row);
        } catch (DataIntegrityViolationException ex) {
            if (eventId != null && !eventId.isBlank()) {
                return inventory.findByEventId(eventId)
                        .map(e -> resolveExistingInsert(e, normalizedPlayer, itemId, eventId))
                        .orElseThrow(() -> new InventoryDomainException(ERR_INSERT));
            }
            throw new InventoryDomainException(ERR_INSERT);
        }

        if (eventId != null && !eventId.isBlank() && cancels.existsById(eventId)) {
            inventory.delete(row);
            throw new InventoryDomainException(ERR_EVENT_CANCELLED);
        }
        return row.getId();
    }

    @Transactional
    public void removeByEvent(String playerId, String itemId, String eventId) {
        if (eventId == null || eventId.isBlank()) {
            throw new InventoryDomainException("error: event_id is required");
        }
        if (cancels.existsById(eventId)) {
            inventory.clearRemoval(eventId);
            throw new InventoryDomainException(ERR_EVENT_CANCELLED);
        }
        if (inventory.existsByRemovedEventId(eventId)) {
            return;
        }

        int updated = inventory.markRemovedByEvent(PlayerIdCodec.format(playerId), itemId, eventId);
        if (updated == 0) {
            if (inventory.existsByRemovedEventId(eventId)) {
                return;
            }
            throw new InventoryDomainException(ERR_ITEM_NOT_FOUND);
        }
        if (cancels.existsById(eventId)) {
            inventory.clearRemoval(eventId);
            throw new InventoryDomainException(ERR_EVENT_CANCELLED);
        }
    }

    @Transactional
    public void rollbackAdded(String inventoryId, String eventId) {
        if (eventId != null && !eventId.isBlank()) {
            cancels.save(InventoryEventCancelEntity.of(eventId));
            inventory.findByEventId(eventId).ifPresent(inventory::delete);
        }
        if (inventoryId != null && !inventoryId.isBlank()) {
            try {
                inventory.deleteById(PlayerIdCodec.parseInventoryId(inventoryId));
            } catch (IllegalArgumentException ignored) {
                // invalid uuid — same as Go ParseObjectId fail
            }
        }
    }

    @Transactional
    public void rollbackRemoved(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            throw new InventoryDomainException("error: event_id is required");
        }
        cancels.save(InventoryEventCancelEntity.of(eventId));
        inventory.clearRemoval(eventId);
    }

    @Transactional(readOnly = true)
    public boolean hasActiveItem(String playerId, String itemId) {
        return inventory.findFirstByPlayerIdAndItemIdAndRemovedEventIdIsNull(
                PlayerIdCodec.format(playerId), itemId).isPresent();
    }

    private UUID resolveExistingInsert(
            PlayerInventoryEntity existing, String playerId, String itemId, String eventId) {
        if (!existing.getPlayerId().equals(playerId)
                || !existing.getItemId().equals(itemId)
                || existing.getRemovedEventId() != null) {
            throw new InventoryDomainException("error: event_id already used");
        }
        if (cancels.existsById(eventId)) {
            inventory.delete(existing);
            throw new InventoryDomainException(ERR_EVENT_CANCELLED);
        }
        return existing.getId();
    }
}
