package com.smartinternshiptracker.auth;

public record AuthResponse(
        AuthUserResponse user,
        String token
) {
}