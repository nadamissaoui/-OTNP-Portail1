package com.monitoring.service;

import com.monitoring.model.PortabilityRequest;
import com.monitoring.repository.PortabilityRequestRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FraudDetectionService {

    private final PortabilityRequestRepository repository;

    public FraudDetectionService(PortabilityRequestRepository repository) {
        this.repository = repository;
    }

    public Map<String, Object> check(String cinNumber, String clientName) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("fraud", false);
        result.put("blocked", false);
        result.put("riskScore", 0);
        result.put("alerts", List.of());
        result.put("message", "CIN disponible");

        if (cinNumber == null || cinNumber.isBlank() || clientName == null || clientName.isBlank()) {
            return result;
        }

        List<PortabilityRequest> byCin = repository.findByCinNumber(cinNumber);
        if (byCin.isEmpty()) return result;

        boolean nameMismatch = byCin.stream()
                .anyMatch(r -> r.getClientName() != null
                        && !r.getClientName().equalsIgnoreCase(clientName)
                        && !"N/A".equalsIgnoreCase(clientName)
                        && !"N/A".equalsIgnoreCase(r.getClientName()));

        if (nameMismatch) {
            String existingName = byCin.stream()
                    .map(PortabilityRequest::getClientName)
                    .filter(n -> n != null && !n.equalsIgnoreCase(clientName))
                    .findFirst().orElse("Inconnu");

            result.put("fraud", true);
            result.put("blocked", true);
            result.put("riskScore", 100);
            result.put("alerts", List.of(Map.of(
                    "type", "CIN_NAME_MISMATCH",
                    "severity", "HIGH",
                    "message", "Le CIN " + cinNumber + " est deja attribue a \"" + existingName
                            + "\" — impossible de l'utiliser avec \"" + clientName + "\""
            )));
            result.put("message", "FRAUDE : CIN deja utilise avec un nom different");
        }

        return result;
    }
}
