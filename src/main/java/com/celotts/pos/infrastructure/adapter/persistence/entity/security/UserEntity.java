package com.celotts.pos.infrastructure.adapter.persistence.entity.security;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "user", schema = "security") // Mapea a la tabla 'user' en el esquema 'security'
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Generación de UUID automático
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash; // Mapea a password_hash en la DB

    @Column(name = "email", unique = true, length = 100)
    private String email;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @ManyToMany(fetch = FetchType.EAGER) // Carga los roles inmediatamente
    @JoinTable(
        name = "user_role", // Nombre de la tabla de unión
        schema = "security", // Esquema de la tabla de unión
        joinColumns = @JoinColumn(name = "user_id"), // Columna de UserEntity en la tabla de unión
        inverseJoinColumns = @JoinColumn(name = "role_id") // Columna de RoleEntity en la tabla de unión
    )
    private Set<RoleEntity> roles = new HashSet<>(); // Inicializar para evitar NullPointerException

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (!this.enabled) { // Asegurar que enabled sea true por defecto si no se setea
            this.enabled = true;
        }
    }
}
