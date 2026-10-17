package com.inwza.player.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KafkaConsumerOffsetJpaRepository extends JpaRepository<KafkaConsumerOffsetEntity, String> {

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = """
                    UPDATE kafka_consumer_offset
                    SET offset_value = GREATEST(offset_value, :offset)
                    WHERE id = :id
                    """,
            nativeQuery = true)
    int upsertMaxOffset(@Param("id") String id, @Param("offset") long offset);
}
