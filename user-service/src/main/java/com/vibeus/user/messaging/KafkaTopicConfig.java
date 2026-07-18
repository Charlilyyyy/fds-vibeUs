package com.vibeus.user.messaging;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Declares Kafka topics owned by the user-service so they are auto-created on
 * startup with explicit partition and replication settings.
 */
@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic userRegisteredTopic() {
        return TopicBuilder.name(KafkaTopics.USER_REGISTERED)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
