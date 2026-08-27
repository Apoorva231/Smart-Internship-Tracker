package com.smartinternshiptracker.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.smartinternshiptracker.user.User;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

class JwtServiceTest {

    @Test
    void generateTokenReturnsSignedTokenForUser() {
        JwtProperties properties = new JwtProperties("01234567890123456789012345678901", 60);
        JwtService jwtService = new JwtService(properties);

        User user = new User(
                "user_123",
                "apoorva@example.com",
                "Apoorva",
                "hashed_password",
                "Montreal, QC"
        );

        String token = jwtService.generateToken(user);

        SecretKey secretKey = new SecretKeySpec(
                properties.secret().getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );

        Jwt decodedToken = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build()
                .decode(token);

        assertFalse(token.isBlank());
        assertEquals("user_123", decodedToken.getSubject());
        assertEquals("apoorva@example.com", decodedToken.getClaimAsString("email"));
        assertEquals("Apoorva", decodedToken.getClaimAsString("name"));
        assertEquals("Montreal, QC", decodedToken.getClaimAsString("city"));
        assertTrue(decodedToken.getExpiresAt().isAfter(Instant.now()));
    }
}