package com.vibeus.user.messaging;

import com.vibeus.user.event.UserRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes {@link UserRegisteredEvent} for decoupled side effects such as
 * welcome notifications, search indexing, or analytics. Kept as a logging stub
 * until those downstream capabilities are implemented.
 */
@Slf4j
@Component
public class UserRegisteredEventListener {

    @KafkaListener(
            topics = KafkaTopics.USER_REGISTERED,
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onUserRegistered(UserRegisteredEvent event) {
        log.info("Received UserRegisteredEvent for userId={} username={} — "
                        + "triggering downstream side effects (notifications/indexing)",
                event.userId(), event.username());
    }
}
