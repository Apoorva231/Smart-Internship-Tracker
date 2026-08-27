package com.smartinternshiptracker.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class JwtConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class)
            .withPropertyValues(
                    "app.jwt.secret=01234567890123456789012345678901",
                    "app.jwt.expiration-minutes=60"
            );

    @Test
    void jwtServiceLoadsWithJwtProperties() {
        contextRunner.run(context -> {
            JwtProperties properties = context.getBean(JwtProperties.class);
            JwtService jwtService = context.getBean(JwtService.class);

            assertNotNull(properties);
            assertNotNull(jwtService);
            assertEquals("01234567890123456789012345678901", properties.secret());
            assertEquals(60, properties.expirationMinutes());
        });
    }

    @Configuration
    @EnableConfigurationProperties(JwtProperties.class)
    static class TestConfig {

        @Bean
        JwtService jwtService(JwtProperties properties) {
            return new JwtService(properties);
        }
    }
}