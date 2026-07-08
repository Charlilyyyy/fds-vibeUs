package com.vibeus.auth.client;

import com.vibeus.auth.dto.request.CreateUserProfileRequest;
import com.vibeus.auth.dto.response.UserProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "user-service",
        url = "${user-service.url}"
)
public interface UserServiceClient {

    @PostMapping("/internal/users")
    UserProfileResponse createUserProfile(@RequestBody CreateUserProfileRequest request);
}
