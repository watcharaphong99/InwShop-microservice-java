package com.inwza.inventory.infrastructure.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerInventoryJpaRepository extends JpaRepository<PlayerInventoryEntity, UUID> {

    long countByPlayerIdAndRemovedEventIdIsNull(String playerId);

    Optional<PlayerInventoryEntity> findByEventId(String eventId);

    boolean existsByRemovedEventId(String removedEventId);

    @Query("""
            SELECT i FROM PlayerInventoryEntity i
            WHERE i.playerId = :playerId
              AND i.removedEventId IS NULL
              AND (:afterId IS NULL OR i.id > :afterId)
            ORDER BY i.id ASC
            """)
    List<PlayerInventoryEntity> findActivePage(
            @Param("playerId") String playerId,
            @Param("afterId") UUID afterId,
            Pageable pageable);

    Optional<PlayerInventoryEntity> findFirstByPlayerIdAndItemIdAndRemovedEventIdIsNull(String playerId, String itemId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE PlayerInventoryEntity i
            SET i.removedEventId = :eventId
            WHERE i.playerId = :playerId
              AND i.itemId = :itemId
              AND i.removedEventId IS NULL
            """)
    int markRemovedByEvent(
            @Param("playerId") String playerId,
            @Param("itemId") String itemId,
            @Param("eventId") String eventId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE PlayerInventoryEntity i
            SET i.removedEventId = NULL
            WHERE i.removedEventId = :eventId
            """)
    int clearRemoval(@Param("eventId") String eventId);
}
