package com.inwza.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CredentialJpaRepository extends JpaRepository<CredentialEntity, UUID> {

    Optional<CredentialEntity> findByAccessToken(String accessToken);
}
