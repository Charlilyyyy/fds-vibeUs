package com.vibeus.user.service.impl;

import com.vibeus.user.dto.request.CreateUserProfileRequest;
import com.vibeus.user.dto.request.UpdateUserProfileRequest;
import com.vibeus.user.dto.response.UserResponse;
import com.vibeus.user.entity.User;
import com.vibeus.user.event.UserRegisteredEvent;
import com.vibeus.user.exception.DuplicateResourceException;
import com.vibeus.user.exception.ResourceNotFoundException;
import com.vibeus.user.mapper.UserMapper;
import com.vibeus.user.messaging.UserEventProducer;
import com.vibeus.user.repository.UserRepository;
import com.vibeus.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserEventProducer userEventProducer;

    @Override
    @Transactional
    public UserResponse createUserProfile(CreateUserProfileRequest request) {

        if (userRepository.existsById(request.id())) {
            throw new DuplicateResourceException("User profile already exists");
        }

        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username already taken");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already registered");
        }

        User user = User.builder()
                .id(request.id())
                .username(request.username())
                .email(request.email())
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        userEventProducer.publishUserRegistered(new UserRegisteredEvent(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                Instant.now()
        ));

        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUserProfile(UUID userId, UpdateUserProfileRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }

        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }

        if (request.bio() != null) {
            user.setBio(request.bio());
        }

        if (request.profileImageUrl() != null) {
            user.setProfileImageUrl(request.profileImageUrl());
        }

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public void deactivateUser(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setActive(false);

        userRepository.save(user);
    }
}
