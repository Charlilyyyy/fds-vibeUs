package com.vibeus.user.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Published to Kafka when a new user profile is created, allowing other
 * services (notifications, search indexing, analytics) to react asynchronously.
 */
public record UserRegisteredEvent(
        UUID userId,
        String username,
        String email,
        Instant occurredAt
) {
}
