package com.smartinternshiptracker.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void registerReturnsCreatedUserEnvelope() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(new AuthResponse(
                        new AuthUserResponse(
                                "user_123",
                                "Apoorva",
                                "apoorva@example.com",
                                "Montreal, QC"
                        ),
                        "jwt_token"
                ));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Apoorva",
                                  "email": "apoorva@example.com",
                                  "password": "Password123!"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.id").value("user_123"))
                .andExpect(jsonPath("$.user.name").value("Apoorva"))
                .andExpect(jsonPath("$.user.email").value("apoorva@example.com"))
                .andExpect(jsonPath("$.user.city").value("Montreal, QC"))
                .andExpect(jsonPath("$.token").value("jwt_token"));

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    void loginReturnsUserEnvelope() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(new AuthResponse(
                        new AuthUserResponse(
                                "user_123",
                                "Apoorva",
                                "apoorva@example.com",
                                "Montreal, QC"
                        ),
                        "jwt_token"
                ));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "apoorva@example.com",
                                  "password": "Password123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id").value("user_123"))
                .andExpect(jsonPath("$.user.name").value("Apoorva"))
                .andExpect(jsonPath("$.user.email").value("apoorva@example.com"))
                .andExpect(jsonPath("$.user.city").value("Montreal, QC"))
                .andExpect(jsonPath("$.token").value("jwt_token"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void registerReturnsConflictForDuplicateEmail() throws Exception {
        doThrow(new DuplicateEmailException())
                .when(authService)
                .register(any(RegisterRequest.class));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Apoorva",
                                  "email": "apoorva@example.com",
                                  "password": "Password123!"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email is already registered"));
    }

    @Test
    void loginReturnsUnauthorizedForInvalidCredentials() throws Exception {
        doThrow(new InvalidLoginException())
                .when(authService)
                .login(any(LoginRequest.class));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "apoorva@example.com",
                                  "password": "WrongPassword123!"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void registerRejectsWeakPassword() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Apoorva",
                                  "email": "apoorva@example.com",
                                  "password": "password123!"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.password").value("Password needs an uppercase letter"));

        verify(authService, never()).register(any(RegisterRequest.class));
    }

    @Test
    void loginRejectsInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "not-an-email",
                                  "password": "Password123!"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.email").isNotEmpty());

        verify(authService, never()).login(any(LoginRequest.class));
    }

}
