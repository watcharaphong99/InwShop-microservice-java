package com.inwza.player.application;

import com.inwza.contracts.player.PlayerProfile;
import com.inwza.player.api.dto.PlayerDtos;
import com.inwza.player.application.messaging.PaymentReplyPublisher;
import com.inwza.player.application.messaging.PaymentTransferMessage;
import com.inwza.player.application.messaging.PlayerTransactionMessage;
import com.inwza.player.application.messaging.RollbackTransactionMessage;
import com.inwza.player.infrastructure.persistence.PlayerEntity;
import com.inwza.player.infrastructure.persistence.PlayerJpaRepository;
import com.inwza.player.infrastructure.persistence.PlayerRoleEntity;
import com.inwza.player.infrastructure.redis.PlayerProfileRedisCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class PlayerApplicationService {

    private static final Logger log = LoggerFactory.getLogger(PlayerApplicationService.class);
    private static final ZoneId BANGKOK = ZoneId.of("Asia/Bangkok");

    private final PlayerJpaRepository players;
    private final PlayerLedgerService ledger;
    private final PaymentReplyPublisher paymentReplies;
    private final PlayerProfileRedisCache profileCache;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public PlayerApplicationService(
            PlayerJpaRepository players,
            PlayerLedgerService ledger,
            PaymentReplyPublisher paymentReplies,
            PlayerProfileRedisCache profileCache) {
        this.players = players;
        this.ledger = ledger;
        this.paymentReplies = paymentReplies;
        this.profileCache = profileCache;
    }

    @Transactional
    public PlayerDtos.PlayerProfileResponse createPlayer(PlayerDtos.CreatePlayerRequest req) {
        if (players.existsByEmailIgnoreCaseOrUsernameIgnoreCase(req.email(), req.username())) {
            throw new PlayerDomainException("error: email or username already exist");
        }

        PlayerEntity entity = new PlayerEntity();
        entity.setEmail(req.email());
        entity.setUsername(req.username());
        entity.setPassword(passwordEncoder.encode(req.password()));
        entity.getRoles().add(PlayerRoleEntity.of(entity, "player", 0));
        players.save(entity);

        return toProfile(entity);
    }

    @Transactional(readOnly = true)
    public PlayerDtos.PlayerProfileResponse findProfile(String playerIdRaw) {
        UUID id = PlayerIdCodec.parseUuid(playerIdRaw);
        PlayerEntity entity = players.findById(id)
                .orElseThrow(() -> new PlayerDomainException("error: player profile not found"));
        return toProfile(entity);
    }

    @Transactional
    public PlayerDtos.SavingAccountResponse addMoney(PlayerDtos.AddMoneyRequest req) {
        UUID txId = ledger.recordTransaction(req.playerId(), BigDecimal.valueOf(req.amount()), null);
        log.debug("manual add money tx {}", txId);
        return getSavingAccount(req.playerId());
    }

    @Transactional(readOnly = true)
    public PlayerDtos.SavingAccountResponse getSavingAccount(String playerId) {
        return new PlayerDtos.SavingAccountResponse(
                PlayerIdCodec.format(playerId), ledger.getBalance(PlayerIdCodec.format(playerId)).doubleValue());
    }

    @Transactional(readOnly = true)
    public PlayerProfile credentialSearch(String email, String password) {
        PlayerEntity entity = players.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new PlayerDomainException("error: email is invalid"));
        if (!passwordEncoder.matches(password, entity.getPassword())) {
            throw new PlayerDomainException("error: password is invalid");
        }
        return toGrpcProfile(entity);
    }

    @Transactional(readOnly = true)
    public PlayerProfile findProfileForRefresh(String playerIdRaw) {
        UUID id = PlayerIdCodec.parseUuid(playerIdRaw);
        PlayerEntity entity = players.findById(id)
                .orElseThrow(() -> new PlayerDomainException("error: player profile not found"));
        return toGrpcProfile(entity);
    }

    public void dockedPlayerMoneyRes(PlayerTransactionMessage req) {
        if (req.eventId() == null || req.eventId().isBlank()) {
            replyBuy(req, "", "error: event_id is required");
            return;
        }
        try {
            UUID txId = ledger.recordTransaction(req.playerId(), BigDecimal.valueOf(req.amount()), req.eventId());
            replyBuy(req, txId.toString(), "");
        } catch (PlayerDomainException ex) {
            replyBuy(req, "", ex.getMessage());
        }
    }

    public void addPlayerMoneyRes(PlayerTransactionMessage req) {
        if (req.eventId() == null || req.eventId().isBlank()) {
            replySell(req, "", "error: event_id is required");
            return;
        }
        try {
            UUID txId = ledger.recordTransaction(req.playerId(), BigDecimal.valueOf(req.amount()), req.eventId());
            replySell(req, txId.toString(), "");
        } catch (PlayerDomainException ex) {
            replySell(req, "", ex.getMessage());
        }
    }

    @Transactional
    public void rollbackPlayerTransaction(RollbackTransactionMessage req) {
        try {
            if (req.eventId() != null && !req.eventId().isBlank()) {
                ledger.cancelByEventId(req.eventId());
            } else if (req.transactionId() != null && !req.transactionId().isBlank()) {
                ledger.deleteTransaction(UUID.fromString(req.transactionId()));
            }
        } catch (Exception ex) {
            log.error("RollbackPlayerTransaction: {}", ex.getMessage());
        }
    }

    private void replyBuy(PlayerTransactionMessage req, String transactionId, String error) {
        paymentReplies.publishBuyReply(PaymentTransferMessage.moneyReply(
                req.eventId(), PlayerIdCodec.format(req.playerId()), req.amount(), transactionId, error));
    }

    private void replySell(PlayerTransactionMessage req, String transactionId, String error) {
        paymentReplies.publishSellReply(PaymentTransferMessage.moneyReply(
                req.eventId(), PlayerIdCodec.format(req.playerId()), req.amount(), transactionId, error));
    }

    private PlayerDtos.PlayerProfileResponse toProfile(PlayerEntity entity) {
        return new PlayerDtos.PlayerProfileResponse(
                entity.getId().toString(),
                entity.getEmail(),
                entity.getUsername(),
                entity.getCreatedAt().atZone(BANGKOK),
                entity.getUpdatedAt().atZone(BANGKOK));
    }

    private PlayerProfile toGrpcProfile(PlayerEntity entity) {
        int roleCode = entity.getRoles().stream().mapToInt(PlayerRoleEntity::getRoleCode).sum();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        ZonedDateTime created = entity.getCreatedAt().atZone(BANGKOK);
        ZonedDateTime updated = entity.getUpdatedAt().atZone(BANGKOK);
        return PlayerProfile.newBuilder()
                .setId(entity.getId().toString())
                .setEmail(entity.getEmail())
                .setUsername(entity.getUsername())
                .setRoleCode(roleCode)
                .setCreateAt(created.format(fmt))
                .setUpdateAt(updated.format(fmt))
                .build();
    }
}
