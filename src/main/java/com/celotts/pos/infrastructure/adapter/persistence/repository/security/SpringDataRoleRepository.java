package com.celotts.pos.infrastructure.adapter.persistence.repository.security;

import com.celotts.pos.infrastructure.adapter.persistence.entity.security.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataRoleRepository extends JpaRepository<RoleEntity, String> {
    Optional<RoleEntity> findByName(String name);
}
