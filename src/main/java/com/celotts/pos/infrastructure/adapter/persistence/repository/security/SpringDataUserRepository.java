package com.celotts.pos.infrastructure.adapter.persistence.repository.security;

import com.celotts.pos.infrastructure.adapter.persistence.entity.security.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataUserRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByUsername(String username);

    // Make sure this line exists in your JPA repository interface:
    boolean existsByUsername(String username);
}
