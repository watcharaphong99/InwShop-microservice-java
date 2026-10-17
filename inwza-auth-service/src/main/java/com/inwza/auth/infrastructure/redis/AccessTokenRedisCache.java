package com.inwza.auth.infrastructure.redis;

import com.inwza.auth.config.JwtProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Optional;

@Component
public class AccessTokenRedisCache {

    private static final String ACCESS_PREFIX = "auth:access:";
    private static final String ROLES_COUNT_KEY = "auth:roles:count";
    private static final Duration ROLES_COUNT_TTL = Duration.ofHours(1);

    private final StringRedisTemplate redis;
    private final Duration accessTtl;

    public AccessTokenRedisCache(StringRedisTemplate redis, JwtProperties jwtProperties) {
        this.redis = redis;
        this.accessTtl = Duration.ofSeconds(jwtProperties.accessDurationSeconds());
    }

    public boolean hasAccessToken(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return false;
        }
        Boolean exists = redis.hasKey(accessTokenKey(accessToken));
        return Boolean.TRUE.equals(exists);
    }

    public void setAccessToken(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return;
        }
        redis.opsForValue().set(accessTokenKey(accessToken), "1", accessTtl);
    }

    public void deleteAccessToken(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return;
        }
        redis.delete(accessTokenKey(accessToken));
    }

    public Optional<Long> getRolesCount() {
        String val = redis.opsForValue().get(ROLES_COUNT_KEY);
        if (val == null) {
            return Optional.empty();
        }
        return Optional.of(Long.parseLong(val));
    }

    public void setRolesCount(long count) {
        redis.opsForValue().set(ROLES_COUNT_KEY, Long.toString(count), ROLES_COUNT_TTL);
    }

    private static String accessTokenKey(String accessToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(accessToken.getBytes(StandardCharsets.UTF_8));
            return ACCESS_PREFIX + HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
