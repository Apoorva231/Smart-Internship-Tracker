package com.smartinternshiptracker.auth;

public record AuthUserResponse(
        String id,
        String name,
        String email,
        String city
) {
}