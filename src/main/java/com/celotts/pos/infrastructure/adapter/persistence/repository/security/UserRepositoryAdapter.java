package com.celotts.pos.infrastructure.adapter.persistence.repository.security;

import com.celotts.pos.application.port.out.security.UserRepositoryPort;
import com.celotts.pos.domain.model.security.User;
import com.celotts.pos.infrastructure.adapter.persistence.entity.security.RoleEntity;
import com.celotts.pos.infrastructure.adapter.persistence.entity.security.UserEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository springDataUserRepository;
    private final SpringDataRoleRepository springDataRoleRepository; // Inyectar para buscar roles

    public UserRepositoryAdapter(SpringDataUserRepository springDataUserRepository, SpringDataRoleRepository springDataRoleRepository) {
        this.springDataUserRepository = springDataUserRepository;
        this.springDataRoleRepository = springDataRoleRepository;
    }

    @Override
    public User save(User user) {
        UserEntity userEntity = toEntity(user);
        UserEntity savedEntity = springDataUserRepository.save(userEntity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<User> findById(String id) {
        return springDataUserRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return springDataUserRepository.findByUsername(username).map(this::toDomain);
    }

    @Override
    public void deleteById(String id) {
        springDataUserRepository.deleteById(id);
    }

    @Override
    public boolean existsByUsername(String username) {
        return springDataUserRepository.existsByUsername(username);
    }

    // --- Mappers entre Dominio y Entidad JPA ---
    private UserEntity toEntity(User user) {
        // Cuando guardamos, la entidad JPA necesita el passwordHash
        UserEntity userEntity = new UserEntity(
                user.getId(),
                user.getUsername(),
                user.getPassword(), // Aquí el password ya debería ser el hash
                user.getEmail(),
                user.isEnabled(),
                null, // createdAt se maneja con @PrePersist en UserEntity
                null,  // lastLogin se maneja en otros flujos
                null   // Los roles se setearán a continuación
        );

        // Mapear roles de String a RoleEntity
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            Set<RoleEntity> roleEntities = user.getRoles().stream()
                    .map(roleName -> springDataRoleRepository.findByName(roleName)
                            .orElseThrow(() -> new RuntimeException("Role not found: " + roleName)))
                    .collect(Collectors.toSet());
            userEntity.setRoles(roleEntities);
        }
        return userEntity;
    }

    private User toDomain(UserEntity userEntity) {
        // Mapear roles de RoleEntity a String
        Set<String> roles = userEntity.getRoles().stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toSet());

        return new User(
                userEntity.getId(),
                userEntity.getUsername(),
                userEntity.getPasswordHash(), // Mapeamos passwordHash de la entidad al password del dominio
                userEntity.getEmail(),
                userEntity.isEnabled(),
                roles // Incluir los roles mapeados
        );
    }

    @Override
    public java.util.List<com.celotts.pos.domain.model.security.User> findAll() {
        return springDataUserRepository.findAll()
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
}
