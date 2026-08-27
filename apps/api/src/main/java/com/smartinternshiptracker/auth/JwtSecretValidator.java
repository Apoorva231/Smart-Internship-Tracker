package com.smartinternshiptracker.auth;

import java.util.Arrays;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class JwtSecretValidator implements ApplicationRunner {

    private final JwtProperties jwtProperties;
    private final Environment environment;

    public JwtSecretValidator(JwtProperties jwtProperties, Environment environment) {
        this.jwtProperties = jwtProperties;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean productionProfileActive = Arrays.asList(environment.getActiveProfiles()).contains("prod");

        if (productionProfileActive && JwtProperties.DEFAULT_DEV_SECRET.equals(jwtProperties.secret())) {
            throw new IllegalStateException("JWT_SECRET must be set when the prod profile is active");
        }
    }
}