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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserEventProducer userEventProducer;

    @InjectMocks
    private UserServiceImpl userService;

    private UUID userId;
    private CreateUserProfileRequest createRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        createRequest = new CreateUserProfileRequest(userId, "jazzfan", "user@example.com");
    }

    @Test
    void createUserProfile_shouldSaveAndPublishEvent() {
        when(userRepository.existsById(userId)).thenReturn(false);
        when(userRepository.existsByUsername("jazzfan")).thenReturn(false);
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toResponse(any(User.class))).thenReturn(sampleResponse());

        UserResponse response = userService.createUserProfile(createRequest);

        assertThat(response.id()).isEqualTo(userId);

        ArgumentCaptor<UserRegisteredEvent> eventCaptor =
                ArgumentCaptor.forClass(UserRegisteredEvent.class);
        verify(userEventProducer).publishUserRegistered(eventCaptor.capture());
        assertThat(eventCaptor.getValue().userId()).isEqualTo(userId);
        assertThat(eventCaptor.getValue().email()).isEqualTo("user@example.com");
    }

    @Test
    void createUserProfile_shouldRejectDuplicateId() {
        when(userRepository.existsById(userId)).thenReturn(true);

        assertThatThrownBy(() -> userService.createUserProfile(createRequest))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userRepository, never()).save(any());
        verify(userEventProducer, never()).publishUserRegistered(any());
    }

    @Test
    void createUserProfile_shouldRejectDuplicateUsername() {
        when(userRepository.existsById(userId)).thenReturn(false);
        when(userRepository.existsByUsername("jazzfan")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUserProfile(createRequest))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserById_shouldReturnProfile() {
        User user = sampleUser();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(sampleResponse());

        UserResponse response = userService.getUserById(userId);

        assertThat(response.username()).isEqualTo("jazzfan");
    }

    @Test
    void getUserById_shouldThrowWhenMissing() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateUserProfile_shouldApplyNonNullFields() {
        User user = sampleUser();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toResponse(any(User.class))).thenReturn(sampleResponse());

        UpdateUserProfileRequest request =
                new UpdateUserProfileRequest("Miles", null, "Trumpet player", null);

        userService.updateUserProfile(userId, request);

        assertThat(user.getFirstName()).isEqualTo("Miles");
        assertThat(user.getBio()).isEqualTo("Trumpet player");
        assertThat(user.getLastName()).isNull();
    }

    @Test
    void deactivateUser_shouldSetActiveFalse() {
        User user = sampleUser();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.deactivateUser(userId);

        assertThat(user.getActive()).isFalse();
        verify(userRepository).save(user);
    }

    private User sampleUser() {
        return User.builder()
                .id(userId)
                .username("jazzfan")
                .email("user@example.com")
                .active(true)
                .build();
    }

    private UserResponse sampleResponse() {
        return new UserResponse(
                userId,
                "jazzfan",
                "user@example.com",
                null,
                null,
                null,
                null,
                true,
                Instant.parse("2026-07-08T12:00:00Z")
        );
    }
}
