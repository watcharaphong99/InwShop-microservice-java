package com.inwza.inventory.application.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RollbackInventoryMessage(
        @JsonProperty("inventory_id") String inventoryId,
        @JsonProperty("player_id") String playerId,
        @JsonProperty("item_id") String itemId,
        @JsonProperty("event_id") String eventId
) {
}
