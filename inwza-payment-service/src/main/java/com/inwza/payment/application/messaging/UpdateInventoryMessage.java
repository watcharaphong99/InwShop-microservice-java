package com.inwza.payment.application.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UpdateInventoryMessage(
        @JsonProperty("player_id") String playerId,
        @JsonProperty("item_id") String itemId,
        @JsonProperty("event_id") String eventId
) {
}
