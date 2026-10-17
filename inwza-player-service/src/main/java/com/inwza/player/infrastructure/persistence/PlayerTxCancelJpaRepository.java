package com.inwza.player.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerTxCancelJpaRepository extends JpaRepository<PlayerTxCancelEntity, String> {
}
