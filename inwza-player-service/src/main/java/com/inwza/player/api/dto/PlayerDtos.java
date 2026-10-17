package com.inwza.player.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.ZonedDateTime;

public final class PlayerDtos {

    private PlayerDtos() {
    }

    public record CreatePlayerRequest(
            @NotBlank @Email @Size(max = 255) String email,
            @NotBlank @Size(max = 32) String password,
            @NotBlank @Size(max = 32) String username
    ) {
    }

    public record AddMoneyRequest(
            @JsonProperty("player_id") String playerId,
            @NotNull double amount,
            @JsonProperty("event_id") String eventId
    ) {
    }

    public record PlayerProfileResponse(
            String id,
            String email,
            String username,
            @JsonProperty("create_at") ZonedDateTime createdAt,
            @JsonProperty("update_at") ZonedDateTime updatedAt
    ) {
    }

    public record SavingAccountResponse(
            @JsonProperty("player_id") String playerId,
            double balance
    ) {
    }

    public record MessageResponse(String message) {
    }
}
