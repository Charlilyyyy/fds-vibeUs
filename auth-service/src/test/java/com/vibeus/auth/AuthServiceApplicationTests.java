package com.vibeus.auth;

import com.vibeus.auth.client.UserServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class AuthServiceApplicationTests {

    @MockitoBean
    private UserServiceClient userServiceClient;

    @Test
    void contextLoads() {
    }
}
