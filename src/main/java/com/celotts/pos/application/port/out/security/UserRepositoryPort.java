package com.celotts.pos.application.port.out.security;

import com.celotts.pos.domain.model.security.User;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findById(String id);
    Optional<User> findByUsername(String username);
    void deleteById(String id);

    List<User> findAll();

    boolean existsByUsername(String username);
}
