package com.monitoring.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Service
public class WsMonitoringClient {

    private final RestTemplate restTemplate;

    @Value("${ws.api.base-url:http://localhost:8089}")
    private String wsBaseUrl;

    public WsMonitoringClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Map<String, Object>> searchDemandes(Long processInstanceId, String crmId, Integer status,
                                                    String msisdn, String phoneNumber, String contractCode,
                                                    String dateDebut, String dateFin, int page, int size) {

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(wsBaseUrl + "/api/monitoring/search")
                .queryParam("page", page)
                .queryParam("size", size);

        if (processInstanceId != null) builder.queryParam("processInstanceId", processInstanceId);
        if (crmId != null && !crmId.isBlank()) builder.queryParam("crmId", crmId);
        if (status != null) builder.queryParam("status", status);
        if (msisdn != null && !msisdn.isBlank()) builder.queryParam("msisdn", msisdn);
        if (phoneNumber != null && !phoneNumber.isBlank()) builder.queryParam("phoneNumber", phoneNumber);
        if (contractCode != null && !contractCode.isBlank()) builder.queryParam("contractCode", contractCode);
        if (dateDebut != null && !dateDebut.isBlank()) builder.queryParam("dateDebut", dateDebut);
        if (dateFin != null && !dateFin.isBlank()) builder.queryParam("dateFin", dateFin);

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );

        return response.getBody() != null ? response.getBody() : List.of();
    }
}
