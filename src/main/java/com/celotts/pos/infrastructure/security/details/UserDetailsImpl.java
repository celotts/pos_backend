package com.celotts.pos.infrastructure.security.details;

import com.celotts.pos.domain.model.security.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private String id;
    private String username;
    private String password;
    private String email;
    private boolean enabled;
    private Collection<? extends GrantedAuthority> authorities;

    // Constructor para crear UserDetails a partir de tu objeto User de dominio
    public static UserDetailsImpl build(User user) {
        // Mapeamos los roles del User de dominio a GrantedAuthority de Spring Security
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(roleName -> new SimpleGrantedAuthority(roleName))
                .collect(Collectors.toList());

        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.isEnabled(),
                authorities
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Por ahora, las cuentas nunca expiran
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Por ahora, las cuentas nunca se bloquean
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Por ahora, las credenciales nunca expiran
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
