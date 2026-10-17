package com.inwza.player.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface PlayerTransactionJpaRepository extends JpaRepository<PlayerTransactionEntity, UUID> {

    Optional<PlayerTransactionEntity> findByEventId(String eventId);

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0) FROM PlayerTransactionEntity t
            WHERE t.playerId = :playerId
            """)
    BigDecimal sumAmountByPlayerId(@Param("playerId") String playerId);
}
