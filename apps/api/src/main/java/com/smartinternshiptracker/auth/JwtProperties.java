package com.smartinternshiptracker.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        long expirationMinutes
) {
    public static final String DEFAULT_DEV_SECRET = "01234567890123456789012345678901";
}
