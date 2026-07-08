package com.vibeus.auth.service.impl;

import com.vibeus.auth.client.UserServiceClient;
import com.vibeus.auth.dto.request.LoginRequest;
import com.vibeus.auth.dto.request.RegisterRequest;
import com.vibeus.auth.dto.request.ValidateTokenRequest;
import com.vibeus.auth.dto.response.AuthenticationResponse;
import com.vibeus.auth.dto.response.RegisterResponse;
import com.vibeus.auth.dto.response.TokenValidationResponse;
import com.vibeus.auth.dto.response.UserProfileResponse;
import com.vibeus.auth.entity.User;
import com.vibeus.auth.exception.DuplicateResourceException;
import com.vibeus.auth.exception.UnauthorizedException;
import com.vibeus.auth.repository.UserRepository;
import com.vibeus.auth.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User("user@example.com", "jazzfan", "encoded-password");
        user.setId(userId);
    }

    @Test
    void register_shouldCreateCredentialsAndProfile() {
        RegisterRequest request = new RegisterRequest("user@example.com", "jazzfan", "password123");

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(userId);
            return saved;
        });
        when(userServiceClient.createUserProfile(any())).thenReturn(
                new UserProfileResponse(userId, "jazzfan", "user@example.com")
        );

        RegisterResponse response = authService.register(request);

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.email()).isEqualTo("user@example.com");
        assertThat(response.username()).isEqualTo("jazzfan");
        verify(userServiceClient).createUserProfile(any());
        verify(userRepository, never()).delete(any());
    }

    @Test
    void register_shouldRejectDuplicateEmail() {
        RegisterRequest request = new RegisterRequest("user@example.com", "jazzfan", "password123");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email");
    }

    @Test
    void register_shouldRejectDuplicateUsername() {
        RegisterRequest request = new RegisterRequest("user@example.com", "jazzfan", "password123");
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByUsername(request.username())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Username");
    }

    @Test
    void register_shouldCompensateWhenProfileCreationFails() {
        RegisterRequest request = new RegisterRequest("user@example.com", "jazzfan", "password123");

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(userId);
            return saved;
        });
        doThrow(new RuntimeException("user-service down"))
                .when(userServiceClient).createUserProfile(any());

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Registration failed");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).delete(userCaptor.capture());
        assertThat(userCaptor.getValue().getId()).isEqualTo(userId);
    }

    @Test
    void login_shouldReturnAccessToken() {
        LoginRequest request = new LoginRequest("user@example.com", "password123");
        Instant expiresAt = Instant.now().plusSeconds(3600);

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.extractExpiration("access-token")).thenReturn(expiresAt);

        AuthenticationResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.expiresAt()).isEqualTo(expiresAt);
    }

    @Test
    void login_shouldRejectUnknownEmail() {
        LoginRequest request = new LoginRequest("missing@example.com", "password123");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void login_shouldRejectWrongPassword() {
        LoginRequest request = new LoginRequest("user@example.com", "wrong-password");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void login_shouldRejectDisabledUser() {
        user.setEnabled(false);
        LoginRequest request = new LoginRequest("user@example.com", "password123");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(true);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void validateToken_shouldReturnClaimsWhenTokenAndUserAreValid() {
        String token = "valid-token";
        Instant expiresAt = Instant.now().plusSeconds(3600);

        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.extractSubject(token)).thenReturn(userId.toString());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(jwtService.extractEmail(token)).thenReturn("user@example.com");
        when(jwtService.extractUsername(token)).thenReturn("jazzfan");
        when(jwtService.extractRoles(token)).thenReturn(List.of("ROLE_USER"));
        when(jwtService.extractExpiration(token)).thenReturn(expiresAt);

        TokenValidationResponse response = authService.validateToken(new ValidateTokenRequest(token));

        assertThat(response.valid()).isTrue();
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.email()).isEqualTo("user@example.com");
        assertThat(response.roles()).containsExactly("ROLE_USER");
    }

    @Test
    void validateToken_shouldReturnInvalidWhenJwtIsInvalid() {
        when(jwtService.isTokenValid("bad-token")).thenReturn(false);

        TokenValidationResponse response = authService.validateToken(new ValidateTokenRequest("bad-token"));

        assertThat(response.valid()).isFalse();
        verify(userRepository, never()).findById(any());
    }

    @Test
    void validateToken_shouldReturnInvalidWhenUserMissing() {
        String token = "valid-token";
        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.extractSubject(token)).thenReturn(userId.toString());
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        TokenValidationResponse response = authService.validateToken(new ValidateTokenRequest(token));

        assertThat(response.valid()).isFalse();
    }
}
