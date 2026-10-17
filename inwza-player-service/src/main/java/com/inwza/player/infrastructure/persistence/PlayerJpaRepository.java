package com.inwza.player.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PlayerJpaRepository extends JpaRepository<PlayerEntity, UUID> {

    boolean existsByEmailIgnoreCaseOrUsernameIgnoreCase(String email, String username);

    Optional<PlayerEntity> findByEmailIgnoreCase(String email);
}
