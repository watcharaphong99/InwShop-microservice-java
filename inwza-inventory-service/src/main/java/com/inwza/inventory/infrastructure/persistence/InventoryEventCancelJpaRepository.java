package com.inwza.inventory.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryEventCancelJpaRepository extends JpaRepository<InventoryEventCancelEntity, String> {
}
