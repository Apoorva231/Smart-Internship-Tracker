package com.smartinternshiptracker.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank
        @Size(min = 2, max = 80)
        String name,

        @NotBlank
        @Email
        @Size(max = 254)
        String email,

        @NotBlank
        @Size(min = 10, max = 72)
        @Pattern(regexp = ".*[a-z].*", message = "Password needs a lowercase letter")
        @Pattern(regexp = ".*[A-Z].*", message = "Password needs an uppercase letter")
        @Pattern(regexp = ".*[0-9].*", message = "Password needs a number")
        @Pattern(regexp = ".*[^A-Za-z0-9].*", message = "Password needs a symbol")
        String password
) {
}