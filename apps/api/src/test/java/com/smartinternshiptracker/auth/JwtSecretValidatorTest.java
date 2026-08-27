package com.smartinternshiptracker.auth;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class JwtSecretValidatorTest {

    @Test
    void allowsDefaultSecretOutsideProduction() {
        JwtSecretValidator validator = new JwtSecretValidator(
                new JwtProperties(JwtProperties.DEFAULT_DEV_SECRET, 60),
                new MockEnvironment()
        );

        assertDoesNotThrow(() -> validator.run(null));
    }

    @Test
    void rejectsDefaultSecretInProduction() {
        JwtSecretValidator validator = new JwtSecretValidator(
                new JwtProperties(JwtProperties.DEFAULT_DEV_SECRET, 60),
                new MockEnvironment().withProperty("spring.profiles.active", "prod")
        );

        assertThrows(IllegalStateException.class, () -> validator.run(null));
    }

    @Test
    void allowsCustomSecretInProduction() {
        JwtSecretValidator validator = new JwtSecretValidator(
                new JwtProperties("production_secret_that_is_not_the_default", 60),
                new MockEnvironment().withProperty("spring.profiles.active", "prod")
        );

        assertDoesNotThrow(() -> validator.run(null));
    }
}