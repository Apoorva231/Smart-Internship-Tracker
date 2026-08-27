package com.smartinternshiptracker.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.smartinternshiptracker.user.User;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

class JwtConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class)
            .withPropertyValues(
                    "app.jwt.secret=01234567890123456789012345678901",
                    "app.jwt.expiration-minutes=60",
                    "app.cors.allowed-origins=http://localhost:5173"
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

    @Test
    void jwtDecoderCanVerifyGeneratedToken() {
        contextRunner.run(context -> {
            JwtService jwtService = context.getBean(JwtService.class);
            JwtDecoder jwtDecoder = context.getBean(JwtDecoder.class);

            User user = new User(
                    "user_123",
                    "apoorva@example.com",
                    "Apoorva",
                    "hashed_password",
                    "Montreal, QC"
            );

            String token = jwtService.generateToken(user);
            Jwt decodedToken = jwtDecoder.decode(token);

            assertEquals("user_123", decodedToken.getSubject());
        });
    }

    @Test
    void corsConfigurationUsesConfiguredAllowedOrigin() {
        contextRunner.run(context -> {
            CorsConfigurationSource source = context.getBean(CorsConfigurationSource.class);

            MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/applications");
            CorsConfiguration configuration = source.getCorsConfiguration(request);

            assertNotNull(configuration);
            assertEquals(List.of("http://localhost:5173"), configuration.getAllowedOrigins());
            assertEquals(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"), configuration.getAllowedMethods());
            assertEquals(List.of("Authorization", "Content-Type"), configuration.getAllowedHeaders());
        });
    }

    @Configuration
    @EnableConfigurationProperties({JwtProperties.class, CorsProperties.class})
    static class TestConfig {

        @Bean
        JwtService jwtService(JwtProperties properties) {
            return new JwtService(properties);
        }

        @Bean
        JwtDecoder jwtDecoder(JwtProperties properties) {
            return new SecurityConfig().jwtDecoder(properties);
        }

        @Bean
        CorsConfigurationSource corsConfigurationSource(CorsProperties properties) {
            return new SecurityConfig().corsConfigurationSource(properties);
        }
    }
}
