package com.inwza.player.application.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record PlayerTransactionMessage(
        @NotBlank @JsonProperty("player_id") String playerId,
        double amount,
        @JsonProperty("event_id") String eventId
) {
}
