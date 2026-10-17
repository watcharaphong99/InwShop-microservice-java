package com.inwza.auth.application;

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
}
