package com.vibeus.user.service;

import com.vibeus.user.dto.request.CreateUserProfileRequest;
import com.vibeus.user.dto.request.UpdateUserProfileRequest;
import com.vibeus.user.dto.response.UserResponse;

import java.util.UUID;

public interface UserService {

    UserResponse createUserProfile(CreateUserProfileRequest request);

    UserResponse getUserById(UUID userId);

    UserResponse updateUserProfile(UUID userId, UpdateUserProfileRequest request);

    void deactivateUser(UUID userId);
}
