package com.monitoring.controller;

import com.monitoring.dto.LogAnalysisResponse;
import com.monitoring.entity.LogEntry;
import com.monitoring.entity.LogEntry.LogLevel;
import com.monitoring.repository.LogEntryRepository;
import com.monitoring.service.AiAnalysisService;
import com.monitoring.service.KieServerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@RestController
@RequestMapping("/api/kie")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class KieServerController {

    private final KieServerService kieServerService;
    private final AiAnalysisService aiAnalysisService;
    private final LogEntryRepository logEntryRepository;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> checkConnection() {
        return ResponseEntity.ok(kieServerService.checkConnection());
    }

    @GetMapping("/containers")
    public ResponseEntity<List<Map<String, Object>>> getContainers() {
        return ResponseEntity.ok(kieServerService.getContainers());
    }

    @GetMapping("/errors")
    public ResponseEntity<List<Map<String, Object>>> getAllErrors() {
        return ResponseEntity.ok(kieServerService.getExecutionErrors());
    }

    @GetMapping("/errors/{containerId}")
    public ResponseEntity<List<Map<String, Object>>> getErrorsByContainer(
            @PathVariable String containerId) {
        return ResponseEntity.ok(kieServerService.getErrorsByContainer(containerId));
    }

    @GetMapping("/instances/error/{containerId}")
    public ResponseEntity<List<Map<String, Object>>> getProcessInstancesInError(
            @PathVariable String containerId) {
        return ResponseEntity.ok(kieServerService.getProcessInstancesInError(containerId));
    }

    @PostMapping("/analyze-error")
    public ResponseEntity<List<LogAnalysisResponse>> analyzeKieError(
            @RequestBody Map<String, Object> kieError) {

        String errorMessage = (String) kieError.getOrDefault("errorMessage", "");
        String processId = (String) kieError.getOrDefault("processId", "UNKNOWN");
        String processName = (String) kieError.getOrDefault("processName", "");
        String containerId = (String) kieError.getOrDefault("containerId", "");
        String activityName = (String) kieError.getOrDefault("activityName", "");
        String activityType = (String) kieError.getOrDefault("activityType", "");
        Object processInstanceIdObj = kieError.get("processInstanceId");
        Object errorDateObj = kieError.get("errorDate");

        String workflowType = "PORT_IN";
        if (containerId.toLowerCase().contains("out") || processId.toLowerCase().contains("out")) {
            workflowType = "PORT_OUT";
        }

        LocalDateTime errorDateTime = LocalDateTime.now();
        if (errorDateObj != null) {
            try {
                long millis = ((Number) errorDateObj).longValue();
                if (millis > 0) {
                    errorDateTime = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(millis), ZoneId.systemDefault());
                }
            } catch (Exception ignored) {
            }
        }

        LogEntry logEntry = new LogEntry();
        logEntry.setProcessId(processInstanceIdObj != null
                ? processName + ":" + processInstanceIdObj
                : processId);
        logEntry.setProcessName(processName);
        logEntry.setWorkflowType(workflowType);
        logEntry.setLogLevel(LogLevel.ERROR);
        logEntry.setMessage(errorMessage);
        logEntry.setErrorType("jBPM Process Error - " + activityName);
        logEntry.setTimestamp(errorDateTime);
        logEntry.setCreatedAt(LocalDateTime.now());
        logEntry.setResolved(false);

        logEntry = logEntryRepository.save(logEntry);

        List<LogAnalysisResponse> results = new ArrayList<>();
        try {
            LogAnalysisResponse response = aiAnalysisService.analyzeErrorWithNode(
                    logEntry, activityName, activityType);
            if (response != null) {
                results.add(response);
            }
        } catch (Exception e) {
            LogAnalysisResponse errorResponse = LogAnalysisResponse.builder()
                    .processId(logEntry.getProcessId())
                    .errorMessage(errorMessage)
                    .severity("ERROR")
                    .build();
            results.add(errorResponse);
        }

        return ResponseEntity.ok(results);
    }
}