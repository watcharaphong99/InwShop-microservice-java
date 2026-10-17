package com.inwza.auth.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.ZonedDateTime;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(
            @NotBlank @Email @Size(max = 255) String email,
            @NotBlank @Size(max = 32) String password
    ) {
    }

    public record RefreshTokenRequest(
            @NotBlank @Size(max = 64) @JsonProperty("credential_id") String credentialId,
            @NotBlank @Size(max = 500) @JsonProperty("refresh_token") String refreshToken
    ) {
    }

    public record LogoutRequest(
            @NotBlank @Size(max = 64) @JsonProperty("credential_id") String credentialId
    ) {
    }

    public record MessageResponse(String message) {
    }

    public record CredentialResponse(
            @JsonProperty("_id") String id,
            @JsonProperty("player_id") String playerId,
            @JsonProperty("role_code") int roleCode,
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("refresh_token") String refreshToken,
            @JsonProperty("created_at") ZonedDateTime createdAt,
            @JsonProperty("updated_at") ZonedDateTime updatedAt
    ) {
    }

    public record ProfileInterceptorResponse(
            String id,
            String email,
            String username,
            @JsonProperty("create_at") ZonedDateTime createdAt,
            @JsonProperty("update_at") ZonedDateTime updatedAt,
            CredentialResponse credential
    ) {
    }
}
