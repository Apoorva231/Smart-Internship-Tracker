package com.smartinternshiptracker.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.smartinternshiptracker.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
@ImportAutoConfiguration({
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class,
        ServletWebSecurityAutoConfiguration.class
})
@TestPropertySource(properties = {
        "app.jwt.secret=01234567890123456789012345678901",
        "app.jwt.expiration-minutes=60",
        "app.cors.allowed-origins=http://localhost:5173"
})
class AuthControllerTest {

    private static final String JWT_SECRET = "01234567890123456789012345678901";

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
    void meReturnsCurrentUserEnvelope() throws Exception {
        when(authService.currentUser("user_123"))
                .thenReturn(new CurrentUserResponse(
                        new AuthUserResponse(
                                "user_123",
                                "Apoorva",
                                "apoorva@example.com",
                                "Montreal, QC"
                        )
                ));

        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id").value("user_123"))
                .andExpect(jsonPath("$.user.name").value("Apoorva"))
                .andExpect(jsonPath("$.user.email").value("apoorva@example.com"))
                .andExpect(jsonPath("$.user.city").value("Montreal, QC"));

        verify(authService).currentUser("user_123");
    }

    @Test
    void meRequiresBearerToken() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Unauthorized"));
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

    private String bearerToken() {
        JwtService jwtService = new JwtService(new JwtProperties(JWT_SECRET, 60));
        User user = new User(
                "user_123",
                "apoorva@example.com",
                "Apoorva",
                "hashed_password",
                "Montreal, QC"
        );

        return "Bearer " + jwtService.generateToken(user);
    }

}
