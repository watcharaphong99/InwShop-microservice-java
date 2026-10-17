package com.inwza.payment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface OrderWorkflowJpaRepository extends JpaRepository<OrderWorkflowEntity, String> {

    @Query(
            """
                    SELECT w FROM OrderWorkflowEntity w
                    WHERE w.status = 'running' AND w.updatedAt < :olderThan
                    """)
    List<OrderWorkflowEntity> findStaleRunning(@Param("olderThan") Instant olderThan);
}
