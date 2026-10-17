package com.inwza.player.application.messaging;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record PaymentTransferMessage(
        @JsonProperty("event_id") String eventId,
        @JsonProperty("inventory_id") String inventoryId,
        @JsonProperty("transaction_id") String transactionId,
        @JsonProperty("player_id") String playerId,
        @JsonProperty("item_id") String itemId,
        double amount,
        String error
) {
    public static PaymentTransferMessage moneyReply(
            String eventId, String playerId, double amount, String transactionId, String error) {
        return new PaymentTransferMessage(eventId, null, transactionId, playerId, null, amount, error);
    }
}
