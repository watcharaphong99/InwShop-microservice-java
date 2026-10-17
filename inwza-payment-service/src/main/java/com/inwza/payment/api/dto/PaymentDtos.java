package com.inwza.payment.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public final class PaymentDtos {

    private PaymentDtos() {
    }

    public record ItemServiceRequest(@NotEmpty @Valid List<ItemLine> items) {
    }

    public record ItemLine(
            @NotBlank @JsonProperty("item_id") String itemId,
            @JsonProperty("price") double price) {
    }

    public record MessageResponse(String message) {
    }
}
