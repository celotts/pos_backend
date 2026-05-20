package com.celotts.pos.infrastructure.security.details.service;

import com.celotts.pos.application.port.in.security.UserServicePort;
import com.celotts.pos.infrastructure.security.details.UserDetailsImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserServicePort userServicePort;

    public UserDetailsServiceImpl(UserServicePort userServicePort) {
        this.userServicePort = userServicePort;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userServicePort.getUserByUsername(username)
                .map(UserDetailsImpl::build) // Convierte tu User de dominio a UserDetailsImpl
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
    }
}
