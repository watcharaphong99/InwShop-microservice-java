package com.inwza.inventory.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public final class InventoryDtos {

    private InventoryDtos() {
    }

    public record ItemShowCase(
            @JsonProperty("item_id") String itemId,
            String title,
            double price,
            int damage,
            @JsonProperty("image_url") String imageUrl
    ) {
    }

    public record ItemInInventory(
            @JsonProperty("inventory_id") String inventoryId,
            @JsonProperty("player_id") String playerId,
            @JsonProperty("item_id") String itemId,
            String title,
            double price,
            int damage,
            @JsonProperty("image_url") String imageUrl
    ) {
        public ItemInInventory(String inventoryId, String playerId, ItemShowCase item) {
            this(
                    inventoryId,
                    playerId,
                    item.itemId(),
                    item.title(),
                    item.price(),
                    item.damage(),
                    item.imageUrl());
        }
    }

    public record PaginateResponse(
            Object data,
            long total,
            int limit,
            FirstLink first,
            NextLink next
    ) {
    }

    public record FirstLink(String href) {
    }

    public record NextLink(String start, String href) {
    }

    public record MessageResponse(String message) {
    }
}
