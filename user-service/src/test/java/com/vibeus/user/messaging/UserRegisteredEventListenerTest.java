package com.vibeus.user.messaging;

import com.vibeus.user.event.UserRegisteredEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;

class UserRegisteredEventListenerTest {

    private final UserRegisteredEventListener listener = new UserRegisteredEventListener();

    @Test
    void onUserRegistered_shouldHandleEventWithoutError() {
        UserRegisteredEvent event = new UserRegisteredEvent(
                UUID.randomUUID(), "jazzfan", "user@example.com", Instant.now());

        assertThatCode(() -> listener.onUserRegistered(event)).doesNotThrowAnyException();
    }
}
