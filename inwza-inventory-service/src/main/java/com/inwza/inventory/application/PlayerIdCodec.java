package com.inwza.inventory.application;

import java.util.UUID;

public final class PlayerIdCodec {

    private static final String PREFIX = "player:";

    private PlayerIdCodec() {
    }

    public static String format(String id) {
        if (id == null || id.isBlank()) {
            return PREFIX;
        }
        return id.startsWith(PREFIX) ? id : PREFIX + id;
    }

    public static String stripPrefix(String playerId) {
        return playerId.startsWith(PREFIX) ? playerId.substring(PREFIX.length()) : playerId;
    }

    public static UUID parseInventoryId(String raw) {
        return UUID.fromString(raw);
    }
}
