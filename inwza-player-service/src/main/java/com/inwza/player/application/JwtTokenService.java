package com.inwza.player.application;

import com.inwza.player.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Service
public class JwtTokenService {

    private final JwtProperties properties;

    public JwtTokenService(JwtProperties properties) {
        this.properties = properties;
    }

    public ParsedAccessToken parseAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key(properties.accessSecretKey()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            if (!"access-token".equals(claims.getSubject())) {
                throw new InvalidTokenException("error: token is invalid");
            }
            String playerId = claims.get("player_id", String.class);
            if (playerId == null || playerId.isBlank()) {
                throw new InvalidTokenException("error: token is invalid");
            }
            return new ParsedAccessToken(playerId);
        } catch (ExpiredJwtException ex) {
            throw new InvalidTokenException("error: token is expired");
        } catch (JwtException ex) {
            throw new InvalidTokenException("error: token is invalid");
        }
    }

    public void validateApiKey(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key(properties.apiSecretKey()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            if (!"api-key".equals(claims.getSubject())) {
                throw new InvalidTokenException("error: token is invalid");
            }
        } catch (JwtException ex) {
            throw new InvalidTokenException("error: token is invalid");
        }
    }

    public String apiKeyToken() {
        return Jwts.builder()
                .header().add("typ", "JWT").and()
                .issuer("Iwpza.com")
                .subject("api-key")
                .audience().add("Iwpza.com").and()
                .claim("player_id", "")
                .claim("role_code", 0)
                .signWith(key(properties.apiSecretKey()))
                .compact();
    }

    private SecretKey key(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public record ParsedAccessToken(String playerId) {
    }

    public static class InvalidTokenException extends RuntimeException {
        public InvalidTokenException(String message) {
            super(message);
        }
    }
}
