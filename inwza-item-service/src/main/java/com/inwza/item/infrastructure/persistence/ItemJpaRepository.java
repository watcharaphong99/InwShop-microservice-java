package com.inwza.item.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItemJpaRepository extends JpaRepository<ItemEntity, UUID>, JpaSpecificationExecutor<ItemEntity> {

    boolean existsByTitleIgnoreCase(String title);

    Optional<ItemEntity> findByTitleIgnoreCase(String title);

    List<ItemEntity> findByIdInAndUsageStatusTrue(Collection<UUID> ids);
}
