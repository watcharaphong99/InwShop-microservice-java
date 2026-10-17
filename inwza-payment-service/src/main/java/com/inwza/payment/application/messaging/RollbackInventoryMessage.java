package com.inwza.payment.application.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RollbackInventoryMessage(
        @JsonProperty("inventory_id") String inventoryId,
        @JsonProperty("event_id") String eventId,
        @JsonProperty("player_id") String playerId,
        @JsonProperty("item_id") String itemId
) {
}
