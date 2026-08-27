package com.smartinternshiptracker.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.smartinternshiptracker.user.User;
import com.smartinternshiptracker.user.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerCreatesUserWithHashedPassword() {
        RegisterRequest request = new RegisterRequest(
                "Apoorva",
                "apoorva@example.com",
                "Password123!"
        );

        when(userRepository.findByEmail("apoorva@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password123!")).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt_token");

        AuthResponse response = authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals("Apoorva", savedUser.getName());
        assertEquals("apoorva@example.com", savedUser.getEmail());
        assertEquals("hashed_password", savedUser.getPasswordHash());
        assertNotEquals("Password123!", savedUser.getPasswordHash());
        assertEquals("Montreal, QC", savedUser.getCity());

        assertEquals(savedUser.getId(), response.user().id());
        assertEquals("Apoorva", response.user().name());
        assertEquals("apoorva@example.com", response.user().email());
        assertEquals("Montreal, QC", response.user().city());
        assertEquals("jwt_token", response.token());
    }

    @Test
    void registerRejectsDuplicateEmail() {
        RegisterRequest request = new RegisterRequest(
                "Apoorva",
                "APOORVA@example.com",
                "Password123!"
        );

        User existingUser = new User(
                "user_existing",
                "apoorva@example.com",
                "Apoorva",
                "hash",
                "Montreal, QC"
        );

        when(userRepository.findByEmail("apoorva@example.com")).thenReturn(Optional.of(existingUser));

        assertThrows(DuplicateEmailException.class, () -> authService.register(request));

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginReturnsUserWhenPasswordMatches() {
        LoginRequest request = new LoginRequest(
                "APOORVA@example.com",
                "Password123!"
        );

        User user = new User(
                "user_123",
                "apoorva@example.com",
                "Apoorva",
                "hashed_password",
                "Montreal, QC"
        );

        when(userRepository.findByEmail("apoorva@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "hashed_password")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt_token");

        AuthResponse response = authService.login(request);

        verify(passwordEncoder).matches("Password123!", "hashed_password");

        assertEquals("user_123", response.user().id());
        assertEquals("Apoorva", response.user().name());
        assertEquals("apoorva@example.com", response.user().email());
        assertEquals("Montreal, QC", response.user().city());
        assertEquals("jwt_token", response.token());
    }

    @Test
    void loginRejectsMissingUser() {
        LoginRequest request = new LoginRequest(
                "missing@example.com",
                "Password123!"
        );

        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(InvalidLoginException.class, () -> authService.login(request));

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void loginRejectsWrongPassword() {
        LoginRequest request = new LoginRequest(
                "apoorva@example.com",
                "WrongPassword123!"
        );

        User user = new User(
                "user_123",
                "apoorva@example.com",
                "Apoorva",
                "hashed_password",
                "Montreal, QC"
        );

        when(userRepository.findByEmail("apoorva@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword123!", "hashed_password")).thenReturn(false);

        assertThrows(InvalidLoginException.class, () -> authService.login(request));

        verify(passwordEncoder).matches("WrongPassword123!", "hashed_password");
    }

    @Test
    void currentUserReturnsUserEnvelope() {
        User user = new User(
                "user_123",
                "apoorva@example.com",
                "Apoorva",
                "hashed_password",
                "Montreal, QC"
        );

        when(userRepository.findById("user_123")).thenReturn(Optional.of(user));

        CurrentUserResponse response = authService.currentUser("user_123");

        assertEquals("user_123", response.user().id());
        assertEquals("Apoorva", response.user().name());
        assertEquals("apoorva@example.com", response.user().email());
        assertEquals("Montreal, QC", response.user().city());
    }

    @Test
    void currentUserRejectsMissingUser() {
        when(userRepository.findById("missing_user")).thenReturn(Optional.empty());

        assertThrows(CurrentUserNotFoundException.class, () -> authService.currentUser("missing_user"));
    }

}
