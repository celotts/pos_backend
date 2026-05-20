package com.celotts.pos.application.service.security;

import com.celotts.pos.application.dto.security.LoginRequest;
import com.celotts.pos.infrastructure.security.jwt.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService; // Para obtener los detalles del usuario después de la autenticación

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    public String authenticateAndGenerateToken(LoginRequest loginRequest) {
        // Autenticar al usuario usando Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.username(),
                        loginRequest.password()
                )
        );

        // Si la autenticación es exitosa, generar el token JWT
        if (authentication.isAuthenticated()) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return jwtService.generateToken(userDetails);
        } else {
            // Esto no debería ocurrir si authentication.isAuthenticated() es true,
            // ya que authenticationManager.authenticate ya lanzaría una excepción si falla.
            throw new RuntimeException("Authentication failed for user: " + loginRequest.username());
        }
    }
}
