package com.celotts.pos.application.dto.security;

public record LoginRequest(
    String username,
    String password
) { }
