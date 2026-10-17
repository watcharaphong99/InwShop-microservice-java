package com.inwza.auth.application;

import com.inwza.auth.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

@Service
public class JwtTokenService {

    private static final String ISSUER = "Iwpza.com";
    private static final List<String> AUDIENCE = List.of("Iwpza.com");
    private static final ZoneId BANGKOK = ZoneId.of("Asia/Bangkok");

    private final JwtProperties properties;
    private final String apiKeyToken;

    public JwtTokenService(JwtProperties properties) {
        this.properties = properties;
        this.apiKeyToken = signApiKey();
    }

    public String apiKeyToken() {
        return apiKeyToken;
    }

    public String signAccessToken(String playerId, int roleCode) {
        return signToken(
                properties.accessSecretKey(),
                "access-token",
                playerId,
                roleCode,
                plusSeconds(properties.accessDurationSeconds()));
    }

    public String signRefreshToken(String playerId, int roleCode) {
        return signToken(
                properties.refreshSecretKey(),
                "refresh-token",
                playerId,
                roleCode,
                plusSeconds(properties.refreshDurationSeconds()));
    }

    public String reloadRefreshToken(String playerId, int roleCode, Instant existingExpiry) {
        Instant now = nowBangkok().toInstant();
        return Jwts.builder()
                .header().add("typ", "JWT").and()
                .issuer(ISSUER)
                .subject("refresh-token")
                .audience().add(AUDIENCE).and()
                .issuedAt(Date.from(now))
                .notBefore(Date.from(now))
                .expiration(Date.from(existingExpiry))
                .claim("player_id", playerId)
                .claim("role_code", roleCode)
                .signWith(key(properties.refreshSecretKey()))
                .compact();
    }

    public ParsedToken parseAccessOrRefresh(String secret, String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key(secret))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String playerId = claims.get("player_id", String.class);
            Integer roleCode = claims.get("role_code", Integer.class);
            if (playerId == null || roleCode == null) {
                throw new InvalidTokenException("error: token is invalid");
            }
            return new ParsedToken(claims.getSubject(), playerId, roleCode, claims.getExpiration().toInstant());
        } catch (ExpiredJwtException ex) {
            throw new InvalidTokenException("error: token is expired");
        } catch (JwtException ex) {
            throw new InvalidTokenException("error: token is invalid");
        }
    }

    public void validateApiKey(String token) {
        ParsedToken parsed = parseAccessOrRefresh(properties.apiSecretKey(), token);
        if (!"api-key".equals(parsed.subject())) {
            throw new InvalidTokenException("error: token is invalid");
        }
    }

    private String signApiKey() {
        return signToken(properties.apiSecretKey(), "api-key", "", 0, plusSeconds(31_560_000L));
    }

    private String signToken(String secret, String subject, String playerId, int roleCode, Instant expiry) {
        Instant now = nowBangkok().toInstant();
        return Jwts.builder()
                .header().add("typ", "JWT").and()
                .issuer(ISSUER)
                .subject(subject)
                .audience().add(AUDIENCE).and()
                .issuedAt(Date.from(now))
                .notBefore(Date.from(now))
                .expiration(Date.from(expiry))
                .claim("player_id", playerId)
                .claim("role_code", roleCode)
                .signWith(key(secret))
                .compact();
    }

    private Instant plusSeconds(long seconds) {
        return nowBangkok().plusSeconds(seconds).toInstant();
    }

    private ZonedDateTime nowBangkok() {
        return ZonedDateTime.now(BANGKOK);
    }

    private SecretKey key(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public record ParsedToken(String subject, String playerId, int roleCode, Instant expiresAt) {
    }
}
