package com.autovyn.app.events;

import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class EventPublisher {
    private final String projectId;
    private final String topicId;

    public EventPublisher(@Value("${gcp.project-id}") String projectId, 
                         @Value("${app.pubsub.topic}") String topicId) {
        this.projectId = projectId;
        this.topicId = topicId;
    }

    public void publish(String type, Map<String, Object> data) {
        try {
            TopicName topicName = TopicName.of(projectId, topicId);
            Publisher publisher = Publisher.newBuilder(topicName).build();
            
            String message = String.format("{\"type\":\"%s\",\"data\":%s}", type, 
                    new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(data));
            
            ByteString dataBytes = ByteString.copyFromUtf8(message);
            PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
                    .setData(dataBytes)
                    .build();
            
            publisher.publish(pubsubMessage);
            publisher.shutdown();
            publisher.awaitTermination(1, TimeUnit.MINUTES);
        } catch (Exception e) {
            // Log error but don't fail the main operation
            System.err.println("Failed to publish event: " + e.getMessage());
        }
    }
}


