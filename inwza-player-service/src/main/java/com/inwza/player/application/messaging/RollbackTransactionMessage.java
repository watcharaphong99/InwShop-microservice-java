package com.inwza.player.application.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RollbackTransactionMessage(
        @JsonProperty("transaction_id") String transactionId,
        @JsonProperty("event_id") String eventId
) {
}
