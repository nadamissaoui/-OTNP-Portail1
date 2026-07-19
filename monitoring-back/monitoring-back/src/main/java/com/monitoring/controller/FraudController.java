package com.monitoring.controller;

import com.monitoring.service.FraudDetectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/fraud")
@CrossOrigin(origins = "http://localhost:3000")
public class FraudController {

    private final FraudDetectionService fraudDetectionService;

    public FraudController(FraudDetectionService fraudDetectionService) {
        this.fraudDetectionService = fraudDetectionService;
    }

    @PostMapping("/check")
    public ResponseEntity<Map<String, Object>> check(@RequestBody Map<String, String> body) {
        String cinNumber = body.getOrDefault("cinNumber", "");
        String clientName = body.getOrDefault("clientName", "");
        return ResponseEntity.ok(fraudDetectionService.check(cinNumber, clientName));
    }
}
