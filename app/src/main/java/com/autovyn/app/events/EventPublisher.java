package com.autovyn.app.events;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class EventPublisher {
    private final WebClient webClient;
    private final String workerUrl;

    public EventPublisher(@Value("${worker.url}") String workerUrl) {
        this.workerUrl = workerUrl;
        this.webClient = WebClient.builder().build();
    }

    public void publish(String type, Map<String, Object> data) {
        try {
            webClient.post()
                    .uri(workerUrl + "/events")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("type", type, "data", data))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception ignored) {
        }
    }
}


