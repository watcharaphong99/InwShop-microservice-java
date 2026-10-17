package com.inwza.payment.application.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PlayerTransactionMessage(
        @JsonProperty("player_id") String playerId,
        double amount,
        @JsonProperty("event_id") String eventId
) {
}
