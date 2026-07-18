package com.vibeus.user.mapper;

import com.vibeus.user.dto.response.UserResponse;
import com.vibeus.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getBio(),
                user.getProfileImageUrl(),
                user.getActive(),
                user.getCreatedAt()
        );
    }
}
