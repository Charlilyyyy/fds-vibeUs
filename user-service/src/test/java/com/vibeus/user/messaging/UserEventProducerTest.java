package com.vibeus.user.messaging;

import com.vibeus.user.event.UserRegisteredEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserEventProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private UserEventProducer userEventProducer;

    @Test
    void publishUserRegistered_shouldSendToTopicKeyedByUserId() {
        UUID userId = UUID.randomUUID();
        UserRegisteredEvent event = new UserRegisteredEvent(
                userId, "jazzfan", "user@example.com", Instant.now());

        userEventProducer.publishUserRegistered(event);

        verify(kafkaTemplate).send(KafkaTopics.USER_REGISTERED, userId.toString(), event);
    }
}
