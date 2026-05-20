package com.celotts.pos.domain.model.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String id;
    private String username;
    private String password; // La contraseña ya cifrada
    private String email;
    private boolean enabled;
    private Set<String> roles = new HashSet<>(); // Añadido: Lista de roles (nombres)
}
