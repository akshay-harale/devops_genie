package com.encora.chat.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @GetMapping(value = "/health")
    public Health getStatus() {
        return Health.builder().status("working").build();
    }

    @GetMapping("/secured")
    public Health securedEndpoint() {
        return Health.builder().status("securedEndpoint").build();
    }
}
