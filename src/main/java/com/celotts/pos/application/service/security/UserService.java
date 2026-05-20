package com.celotts.pos.application.service.security;

import com.celotts.pos.application.port.in.security.UserServicePort;
import com.celotts.pos.application.port.out.security.UserRepositoryPort;
import com.celotts.pos.domain.model.security.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService implements UserServicePort {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepositoryPort userRepositoryPort, PasswordEncoder passwordEncoder) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User createUser(User user) {
        // Asegurarse de que el ID se genere si no existe
        String userId = (user.getId() == null || user.getId().isEmpty()) ? UUID.randomUUID().toString() : user.getId();
        
        // Cifrar la contraseña antes de guardar
        String encodedPassword = passwordEncoder.encode(user.getPassword());

        // Asegurar un rol por defecto si no se proporcionan roles
        Set<String> roles = user.getRoles();
        if (roles == null || roles.isEmpty()) {
            roles = new HashSet<>(Collections.singletonList("USER")); // Rol por defecto
        }

        // Crear una nueva instancia de User con los datos actualizados
        User userToSave = new User(
            userId,
            user.getUsername(),
            encodedPassword,
            user.getEmail(),
            user.isEnabled(),
            roles
        );

        return userRepositoryPort.save(userToSave);
    }

    @Override
    public Optional<User> getUserById(String id) {
        return userRepositoryPort.findById(id);
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        return userRepositoryPort.findByUsername(username);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepositoryPort.findAll();
    }

    @Override
    public User updateUser(String id, User updatedUser) {
        return userRepositoryPort.findById(id).map(existingUser -> {
            // Actualizar solo los campos permitidos
            existingUser.setUsername(updatedUser.getUsername());
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            }
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setEnabled(updatedUser.isEnabled());
            
            // Actualizar roles si se proporcionan en updatedUser
            if (updatedUser.getRoles() != null && !updatedUser.getRoles().isEmpty()) {
                existingUser.setRoles(updatedUser.getRoles());
            } else if (existingUser.getRoles() == null || existingUser.getRoles().isEmpty()) {
                // Si no se proporcionan roles en updatedUser y el usuario existente no tiene, asignar por defecto
                existingUser.setRoles(new HashSet<>(Collections.singletonList("USER")));
            }

            return userRepositoryPort.save(existingUser);
        }).orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Override
    public void deleteUser(String id) {
        userRepositoryPort.deleteById(id);
    }
}
