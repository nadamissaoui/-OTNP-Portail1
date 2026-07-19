package com.monitoring.controller;

import com.monitoring.dto.LifecycleAnalysisDTO;
import com.monitoring.model.PortabilityRequest;
import com.monitoring.repository.PortabilityRequestRepository;
import com.monitoring.repository.ProcessInstanceRepository;
import com.monitoring.service.LifecycleAnalysisService;
import com.monitoring.service.WsMonitoringClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.monitoring.service.PredictionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor // Gère automatiquement l'injection des variables 'final' ci-dessous
@CrossOrigin(origins = "http://localhost:3000")
public class MonitoringController {

    private final WsMonitoringClient wsMonitoringClient;
    private final ProcessInstanceRepository processInstanceRepository;
    private final LifecycleAnalysisService lifecycleAnalysisService;
    private final PortabilityRequestRepository portabilityRequestRepository;
    private final PredictionService predictionService;
    private final RestTemplate restTemplate;
    @Value("${ws.api.base-url:http://localhost:8089}") private String wsBaseUrl;
    @Value("${ai.python.service-url:http://localhost:5001}") private String aiBaseUrl;

    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchDemandes(
            @RequestParam(required = false) Long processInstanceId,
            @RequestParam(required = false) String crmId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String msisdn,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String contractCode,
            @RequestParam(required = false) String dateDebut,
            @RequestParam(required = false) String dateFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {

        List<Map<String, Object>> results = wsMonitoringClient.searchDemandes(
                processInstanceId, crmId, status, msisdn, phoneNumber, contractCode,
                dateDebut, dateFin, page, size);

        enrichWithPortabilityData(results);

        return ResponseEntity.ok(results);
    }

    private void enrichWithPortabilityData(List<Map<String, Object>> results) {
        for (Map<String, Object> row : results) {
            Object idObj = row.get("id");
            if (idObj == null) continue;
            Long pid = idObj instanceof Number ? ((Number) idObj).longValue() : null;
            if (pid == null) continue;

            PortabilityRequest pr = portabilityRequestRepository.findByProcessInstanceId(pid).orElse(null);
            if (pr == null) continue;

            Map<String, Object> vars = (Map<String, Object>) row.computeIfAbsent("variables", k -> new HashMap<String, Object>());

            setIfAbsent(vars, "msisdn", pr.getMsisdn());
            setIfAbsent(vars, "contractType", pr.getContractType());
            setIfAbsent(vars, "contractCode", pr.getContractType());
            setIfAbsent(vars, "clientName", pr.getClientName());
            setIfAbsent(vars, "cinNumber", pr.getCinNumber());
            setIfAbsent(vars, "idClient", pr.getIdClient());
            setIfAbsent(vars, "refCrm", pr.getRefCrm());
            setIfAbsent(vars, "marche", pr.getMarche());
            setIfAbsent(vars, "numeroOrange", pr.getNumeroOrange());
            setIfAbsent(vars, "typeIdentite", pr.getTypeIdentite());
        }
    }

    private void setIfAbsent(Map<String, Object> map, String key, String value) {
        if (value != null && !value.isBlank()) {
            map.putIfAbsent(key, value);
        }
    }

    @GetMapping("/tasks/pending")
    public ResponseEntity<List<Map<String, Object>>> getPendingTasks() {
        // 3. Utilisation de la bonne variable injectée et de la méthode du repository
        List<Object[]> results = processInstanceRepository.getPendingHumanTasksStatistics();
        List<Map<String, Object>> response = new ArrayList<>();

        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("taskName", row[0]);
            map.put("count", row[1]);
            response.add(map);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/lifecycle/analysis")
    public ResponseEntity<LifecycleAnalysisDTO> getLifecycleAnalysis() {
        LifecycleAnalysisDTO analysis = lifecycleAnalysisService.getLifecycleAnalysis();
        return ResponseEntity.ok(analysis);
    }

    @GetMapping("/predict")
    public ResponseEntity<Map<String, Object>> predictNextMonth() {
        return ResponseEntity.ok(predictionService.predict());
    }

    @GetMapping("/health-check")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("service", "monitoring-back");
        status.put("status", "UP");
        status.put("timestamp", System.currentTimeMillis());

        Map<String, Object> checks = new LinkedHashMap<>();
        checks.put("self", "UP");

        try {
            restTemplate.getForEntity(wsBaseUrl + "/api/monitoring/statistics/performance", String.class);
            checks.put("otnp-ws1", "UP");
        } catch (Exception e) {
            checks.put("otnp-ws1", "DOWN (" + e.getMessage() + ")");
        }

        try {
            restTemplate.getForEntity(aiBaseUrl + "/health", String.class);
            checks.put("ai-service", "UP");
        } catch (Exception e) {
            checks.put("ai-service", "DOWN (" + e.getMessage() + ")");
        }

        try {
            processInstanceRepository.countInstancesToday();
            checks.put("mysql", "UP");
        } catch (Exception e) {
            checks.put("mysql", "DOWN (" + e.getMessage() + ")");
        }

        status.put("checks", checks);
        boolean allUp = checks.values().stream().allMatch(v -> v.equals("UP"));
        status.put("global", allUp ? "UP" : "DEGRADED");
        return ResponseEntity.ok(status);
    }
}
