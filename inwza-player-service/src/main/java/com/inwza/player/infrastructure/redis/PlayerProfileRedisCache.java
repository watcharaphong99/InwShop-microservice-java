package com.inwza.player.infrastructure.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class PlayerProfileRedisCache {

    private static final Duration TTL = Duration.ofHours(24);
    private static final String PREFIX = "player:profile:";

    private final StringRedisTemplate redis;

    public PlayerProfileRedisCache(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void evict(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            return;
        }
        redis.delete(PREFIX + playerId);
    }

    public void touch(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            return;
        }
        redis.opsForValue().set(PREFIX + playerId, "1", TTL);
    }
}
