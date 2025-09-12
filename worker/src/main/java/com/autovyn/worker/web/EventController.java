package com.autovyn.worker.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

@RestController
public class EventController {
    private static final Logger log = LoggerFactory.getLogger(EventController.class);
    private final ConcurrentLinkedQueue<Map<String, Object>> events = new ConcurrentLinkedQueue<>();

    @PostMapping("/events")
    public ResponseEntity<Map<String, Object>> receive(@RequestBody Map<String, Object> event) {
        event.put("receivedAt", Instant.now().toString());
        events.add(event);
        log.info("Worker received event: {}", event);
        return ResponseEntity.accepted().body(Map.of("status", "accepted"));
    }

    @GetMapping("/events")
    public ResponseEntity<Object> list() {
        return ResponseEntity.ok(events.toArray());
    }
}


