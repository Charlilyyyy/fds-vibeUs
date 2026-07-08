package com.vibeus.auth.service.impl;

import com.vibeus.auth.client.UserServiceClient;
import com.vibeus.auth.dto.request.CreateUserProfileRequest;
import com.vibeus.auth.dto.request.LoginRequest;
import com.vibeus.auth.dto.request.RegisterRequest;
import com.vibeus.auth.dto.response.AuthenticationResponse;
import com.vibeus.auth.dto.response.RegisterResponse;
import com.vibeus.auth.entity.User;
import com.vibeus.auth.exception.DuplicateResourceException;
import com.vibeus.auth.exception.UnauthorizedException;
import com.vibeus.auth.repository.UserRepository;
import com.vibeus.auth.service.AuthService;
import com.vibeus.auth.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserServiceClient userServiceClient;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email is already registered");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username is already taken");
        }

        User user = new User(
                request.email(),
                request.username(),
                passwordEncoder.encode(request.password())
        );
        userRepository.save(user);

        try {
            userServiceClient.createUserProfile(new CreateUserProfileRequest(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail()
            ));
        } catch (Exception ex) {
            log.error("Failed to create user profile for userId={}; compensating by deleting credentials",
                    user.getId(), ex);
            userRepository.delete(user);
            throw new IllegalStateException("Registration failed while creating user profile", ex);
        }

        return new RegisterResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                "Registration successful"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AuthenticationResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (!user.isEnabled()) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String accessToken = jwtService.generateAccessToken(user);

        return new AuthenticationResponse(
                accessToken,
                "Bearer",
                jwtService.extractExpiration(accessToken),
                user.getId()
        );
    }
}
