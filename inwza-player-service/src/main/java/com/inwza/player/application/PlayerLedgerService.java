package com.inwza.player.application;

import com.inwza.player.infrastructure.persistence.PlayerTransactionEntity;
import com.inwza.player.infrastructure.persistence.PlayerTransactionJpaRepository;
import com.inwza.player.infrastructure.persistence.PlayerTxCancelEntity;
import com.inwza.player.infrastructure.persistence.PlayerTxCancelJpaRepository;
import com.inwza.player.infrastructure.persistence.PlayerWalletEntity;
import com.inwza.player.infrastructure.persistence.PlayerWalletJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PlayerLedgerService {

    public static final String ERR_NOT_ENOUGH_MONEY = "error: not enough money";
    public static final String ERR_EVENT_CANCELLED = "error: event cancelled";
    public static final String ERR_INSERT_TX = "error: insert one player transaction failed";
    public static final String ERR_EVENT_ID_USED = "error: event_id already used";

    private final PlayerTransactionJpaRepository transactions;
    private final PlayerWalletJpaRepository wallets;
    private final PlayerTxCancelJpaRepository cancels;

    public PlayerLedgerService(
            PlayerTransactionJpaRepository transactions,
            PlayerWalletJpaRepository wallets,
            PlayerTxCancelJpaRepository cancels) {
        this.transactions = transactions;
        this.wallets = wallets;
        this.cancels = cancels;
    }

    @Transactional
    public UUID recordTransaction(String playerId, BigDecimal amount, String eventId) {
        String normalizedPlayerId = PlayerIdCodec.format(playerId);

        if (eventId != null && !eventId.isBlank() && cancels.existsById(eventId)) {
            throw new PlayerDomainException(ERR_EVENT_CANCELLED);
        }

        if (eventId != null && !eventId.isBlank()) {
            var existing = transactions.findByEventId(eventId);
            if (existing.isPresent()) {
                PlayerTransactionEntity tx = existing.get();
                if (!sameEvent(tx, normalizedPlayerId, amount)) {
                    throw new PlayerDomainException(ERR_EVENT_ID_USED);
                }
                if (cancels.existsById(eventId)) {
                    throw new PlayerDomainException(ERR_EVENT_CANCELLED);
                }
                return tx.getId();
            }
        }

        ensureWallet(normalizedPlayerId);
        int updated = wallets.applyDelta(normalizedPlayerId, amount);
        if (updated == 0) {
            if (amount.signum() < 0) {
                throw new PlayerDomainException(ERR_NOT_ENOUGH_MONEY);
            }
            throw new PlayerDomainException(ERR_INSERT_TX);
        }

        PlayerTransactionEntity tx = new PlayerTransactionEntity();
        tx.setPlayerId(normalizedPlayerId);
        tx.setAmount(amount);
        if (eventId != null && !eventId.isBlank()) {
            tx.setEventId(eventId);
        }
        try {
            transactions.save(tx);
        } catch (Exception ex) {
            wallets.applyDelta(normalizedPlayerId, amount.negate());
            throw new PlayerDomainException(ERR_INSERT_TX);
        }

        if (eventId != null && !eventId.isBlank() && cancels.existsById(eventId)) {
            transactions.delete(tx);
            wallets.applyDelta(normalizedPlayerId, amount.negate());
            throw new PlayerDomainException(ERR_EVENT_CANCELLED);
        }

        return tx.getId();
    }

    @Transactional
    public void cancelByEventId(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            return;
        }
        cancels.save(PlayerTxCancelEntity.of(eventId));
        transactions.findByEventId(eventId).ifPresent(tx -> {
            transactions.delete(tx);
            wallets.applyDelta(tx.getPlayerId(), tx.getAmount().negate());
        });
    }

    @Transactional
    public void deleteTransaction(UUID transactionId) {
        PlayerTransactionEntity tx = transactions.findById(transactionId)
                .orElseThrow(() -> new PlayerDomainException("error: item not found"));
        transactions.delete(tx);
        wallets.applyDelta(tx.getPlayerId(), tx.getAmount().negate());
    }

    @Transactional(readOnly = true)
    public BigDecimal getBalance(String playerId) {
        String normalized = PlayerIdCodec.format(playerId);
        return wallets.findByPlayerId(normalized)
                .map(PlayerWalletEntity::getBalance)
                .orElseGet(() -> transactions.sumAmountByPlayerId(normalized));
    }

    private void ensureWallet(String playerId) {
        if (wallets.findByPlayerId(playerId).isPresent()) {
            return;
        }
        BigDecimal ledgerBalance = transactions.sumAmountByPlayerId(playerId);
        PlayerWalletEntity wallet = new PlayerWalletEntity();
        wallet.setPlayerId(playerId);
        wallet.setBalance(ledgerBalance);
        wallets.save(wallet);
    }

    private static boolean sameEvent(PlayerTransactionEntity existing, String playerId, BigDecimal amount) {
        return existing.getPlayerId().equals(playerId) && existing.getAmount().compareTo(amount) == 0;
    }
}
