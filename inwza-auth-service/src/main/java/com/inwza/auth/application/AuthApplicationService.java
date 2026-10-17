package com.inwza.auth.application;

import com.inwza.auth.api.dto.AuthDtos;
import com.inwza.auth.config.JwtProperties;
import com.inwza.auth.infrastructure.grpc.PlayerGrpcGateway;
import com.inwza.auth.infrastructure.persistence.CredentialEntity;
import com.inwza.auth.infrastructure.persistence.CredentialJpaRepository;
import com.inwza.auth.infrastructure.persistence.RoleJpaRepository;
import com.inwza.auth.infrastructure.redis.AccessTokenRedisCache;
import com.inwza.contracts.auth.AccessTokenSearchRes;
import com.inwza.contracts.auth.RolesCountRes;
import com.inwza.contracts.player.PlayerProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.UUID;

@Service
public class AuthApplicationService {

    private static final ZoneId BANGKOK = ZoneId.of("Asia/Bangkok");

    private final CredentialJpaRepository credentials;
    private final RoleJpaRepository roles;
    private final JwtTokenService jwtTokenService;
    private final JwtProperties jwtProperties;
    private final AccessTokenRedisCache tokenCache;
    private final PlayerGrpcGateway playerGrpc;

    public AuthApplicationService(
            CredentialJpaRepository credentials,
            RoleJpaRepository roles,
            JwtTokenService jwtTokenService,
            JwtProperties jwtProperties,
            AccessTokenRedisCache tokenCache,
            PlayerGrpcGateway playerGrpc) {
        this.credentials = credentials;
        this.roles = roles;
        this.jwtTokenService = jwtTokenService;
        this.jwtProperties = jwtProperties;
        this.tokenCache = tokenCache;
        this.playerGrpc = playerGrpc;
    }

    @Transactional
    public AuthDtos.ProfileInterceptorResponse login(AuthDtos.LoginRequest req) {
        PlayerProfile profile = playerGrpc.credentialSearch(req.email(), req.password());
        return issueCredential(profile);
    }

    @Transactional
    public AuthDtos.ProfileInterceptorResponse refresh(AuthDtos.RefreshTokenRequest req) {
        JwtTokenService.ParsedToken claims = jwtTokenService.parseAccessOrRefresh(
                jwtProperties.refreshSecretKey(), req.refreshToken());

        if (!"refresh-token".equals(claims.subject())) {
            throw new InvalidTokenException("error: token is invalid");
        }

        UUID credentialId = parseCredentialId(req.credentialId());
        CredentialEntity credential = credentials.findById(credentialId)
                .orElseThrow(() -> new CredentialNotFoundException("error: find one player credential failed"));

        if (!credential.getRefreshToken().equals(req.refreshToken())) {
            throw new InvalidTokenException("error: refresh token is invalid");
        }

        PlayerProfile profile = playerGrpc.findProfileForRefresh(PlayerIdCodec.stripPrefix(claims.playerId()));

        String playerId = PlayerIdCodec.format(profile.getId());
        String accessToken = jwtTokenService.signAccessToken(playerId, profile.getRoleCode());
        String refreshToken = jwtTokenService.reloadRefreshToken(
                playerId, profile.getRoleCode(), claims.expiresAt());

        String oldAccess = credential.getAccessToken();
        credential.setPlayerId(playerId);
        credential.setRoleCode(profile.getRoleCode());
        credential.setAccessToken(accessToken);
        credential.setRefreshToken(refreshToken);
        credentials.save(credential);

        tokenCache.deleteAccessToken(oldAccess);
        tokenCache.setAccessToken(accessToken);

        return toProfileResponse(profile, credential);
    }

    @Transactional
    public long logout(String credentialIdRaw) {
        UUID credentialId = parseCredentialId(credentialIdRaw);
        CredentialEntity credential = credentials.findById(credentialId).orElse(null);
        if (credential != null) {
            tokenCache.deleteAccessToken(credential.getAccessToken());
            credentials.deleteById(credentialId);
            return 1L;
        }
        throw new CredentialNotFoundException("error: player credential not found");
    }

    @Transactional(readOnly = true)
    public AccessTokenSearchRes accessTokenSearch(String accessToken) {
        if (tokenCache.hasAccessToken(accessToken)) {
            return AccessTokenSearchRes.newBuilder().setIsValid(true).build();
        }

        CredentialEntity credential = credentials.findByAccessToken(accessToken).orElse(null);
        if (credential == null) {
            return AccessTokenSearchRes.newBuilder().setIsValid(false).build();
        }

        tokenCache.setAccessToken(accessToken);
        return AccessTokenSearchRes.newBuilder().setIsValid(true).build();
    }

    @Transactional(readOnly = true)
    public RolesCountRes rolesCount() {
        return tokenCache.getRolesCount()
                .map(count -> RolesCountRes.newBuilder().setCount(count).build())
                .orElseGet(() -> {
                    long count = roles.count();
                    tokenCache.setRolesCount(count);
                    return RolesCountRes.newBuilder().setCount(count).build();
                });
    }

    private AuthDtos.ProfileInterceptorResponse issueCredential(PlayerProfile profile) {
        String playerId = PlayerIdCodec.format(profile.getId());
        String accessToken = jwtTokenService.signAccessToken(playerId, profile.getRoleCode());
        String refreshToken = jwtTokenService.signRefreshToken(playerId, profile.getRoleCode());

        CredentialEntity credential = new CredentialEntity();
        credential.setPlayerId(playerId);
        credential.setRoleCode(profile.getRoleCode());
        credential.setAccessToken(accessToken);
        credential.setRefreshToken(refreshToken);
        credentials.save(credential);

        tokenCache.setAccessToken(accessToken);
        return toProfileResponse(profile, credential);
    }

    private AuthDtos.ProfileInterceptorResponse toProfileResponse(PlayerProfile profile, CredentialEntity credential) {
        return new AuthDtos.ProfileInterceptorResponse(
                PlayerIdCodec.format(profile.getId()),
                profile.getEmail(),
                profile.getUsername(),
                parseProfileTime(profile.getCreateAt()),
                parseProfileTime(profile.getUpdateAt()),
                new AuthDtos.CredentialResponse(
                        credential.getId().toString(),
                        credential.getPlayerId(),
                        credential.getRoleCode(),
                        credential.getAccessToken(),
                        credential.getRefreshToken(),
                        toBangkok(credential.getCreatedAt()),
                        toBangkok(credential.getUpdatedAt())
                )
        );
    }

    private ZonedDateTime parseProfileTime(String raw) {
        if (raw == null || raw.isBlank()) {
            return ZonedDateTime.now(BANGKOK);
        }
        try {
            return Instant.parse(raw).atZone(BANGKOK);
        } catch (DateTimeParseException ex) {
            return ZonedDateTime.now(BANGKOK);
        }
    }

    private ZonedDateTime toBangkok(Instant instant) {
        return instant.atZone(BANGKOK);
    }

    private UUID parseCredentialId(String raw) {
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ex) {
            throw new CredentialNotFoundException("error: credential id is invalid");
        }
    }
}
