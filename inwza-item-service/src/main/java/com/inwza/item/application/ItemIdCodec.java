package com.inwza.item.application;

import java.util.UUID;

public final class ItemIdCodec {

    private static final String PREFIX = "item:";

    private ItemIdCodec() {
    }

    public static String toExternal(UUID id) {
        return PREFIX + id;
    }

    public static UUID parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("error: id is invalid");
        }
        String value = raw.startsWith(PREFIX) ? raw.substring(PREFIX.length()) : raw;
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("error: id is invalid");
        }
    }
}
