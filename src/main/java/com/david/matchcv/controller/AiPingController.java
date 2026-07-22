package com.david.matchcv.controller;

import java.util.Map;

import com.david.matchcv.service.AiPingService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiPingController {

    private final AiPingService aiPingService;

    public AiPingController(AiPingService aiPingService) {
        this.aiPingService = aiPingService;
    }

    @GetMapping("/api/ai/ping")
    public Map<String, String> ping() {
        // Só o caminho feliz. Falhas viram AiException e são tratadas no GlobalExceptionHandler.
        return Map.of("reply", aiPingService.ping());
    }
}
