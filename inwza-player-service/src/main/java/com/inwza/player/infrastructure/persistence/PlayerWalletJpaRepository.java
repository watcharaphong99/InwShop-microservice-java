package com.inwza.player.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface PlayerWalletJpaRepository extends JpaRepository<PlayerWalletEntity, String> {

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = """
                    UPDATE player_wallets
                    SET balance = balance + :delta
                    WHERE player_id = :playerId
                      AND (:delta >= 0 OR balance >= ABS(:delta))
                    """,
            nativeQuery = true)
    int applyDelta(@Param("playerId") String playerId, @Param("delta") BigDecimal delta);

    Optional<PlayerWalletEntity> findByPlayerId(String playerId);
}
