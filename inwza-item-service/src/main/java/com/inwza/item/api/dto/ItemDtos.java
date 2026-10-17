package com.inwza.item.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class ItemDtos {

    private ItemDtos() {
    }

    public record CreateItemRequest(
            @NotBlank @Size(max = 64) String title,
            @NotNull @PositiveOrZero Double price,
            @NotNull @PositiveOrZero Integer damage,
            @NotBlank @Size(max = 255) @JsonProperty("image_url") String imageUrl
    ) {
    }

    public record ItemShowCase(
            @JsonProperty("item_id") String itemId,
            String title,
            double price,
            int damage,
            @JsonProperty("image_url") String imageUrl
    ) {
    }

    public record ItemUpdateRequest(
            @Size(max = 64) String title,
            @PositiveOrZero Double price,
            @Size(max = 255) @JsonProperty("image_url") String imageUrl,
            @PositiveOrZero Integer damage
    ) {
    }

    public record EnableOrDisableRequest(
            @NotNull @JsonProperty("usage_status") Boolean usageStatus
    ) {
    }

    public record PaginateResponse(
            long total,
            List<ItemShowCase> records,
            String next
    ) {
    }

    public record MessageResponse(String message) {
    }
}
