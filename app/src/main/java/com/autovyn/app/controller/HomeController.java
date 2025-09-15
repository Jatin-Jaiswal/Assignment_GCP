package com.autovyn.app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> home() {
        return Map.of(
            "message", "Welcome to AutoVyn API",
            "version", "1.0.0",
            "status", "running",
            "endpoints", Map.of(
                "health", "/health/ready",
                "items", "/v1/items",
                "actuator", "/actuator/health"
            )
        );
    }
}
