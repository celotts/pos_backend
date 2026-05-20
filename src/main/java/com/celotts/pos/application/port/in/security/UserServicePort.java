package com.celotts.pos.application.port.in.security;

import com.celotts.pos.domain.model.security.User;

import java.util.List;
import java.util.Optional;

public interface UserServicePort {
    User createUser(User user);
    Optional<User> getUserById(String id);
    Optional<User> getUserByUsername(String username);
    List<User> getAllUsers();
    User updateUser(String id, User user);
    void deleteUser(String id);
}
