package com.monitoring.service;

import com.monitoring.model.PortabilityRequest;
import com.monitoring.entity.SmsLog;
import com.monitoring.repository.PortabilityRequestRepository;
import com.monitoring.repository.SmsLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SmsScheduledService {

    private static final Logger log = LoggerFactory.getLogger(SmsScheduledService.class);

    private final RestTemplate restTemplate;
    private final SmsLogRepository smsLogRepository;
    private final PortabilityRequestRepository portabilityRequestRepository;
    private final SmsSenderService smsSenderService;

    @Value("${ws.api.base-url:http://localhost:8089}")
    private String wsBaseUrl;

    private LocalDateTime lastRun;
    private int sentCount;
    private int completedFound;

    public SmsScheduledService(RestTemplate restTemplate, SmsLogRepository smsLogRepository,
                               PortabilityRequestRepository portabilityRequestRepository,
                               SmsSenderService smsSenderService) {
        this.restTemplate = restTemplate;
        this.smsLogRepository = smsLogRepository;
        this.portabilityRequestRepository = portabilityRequestRepository;
        this.smsSenderService = smsSenderService;
    }

    @Scheduled(fixedDelay = 30000)
    public void checkAndSendSms() {
        lastRun = LocalDateTime.now();
        sentCount = 0;
        completedFound = 0;

        try {
            String url = wsBaseUrl + "/api/monitoring/search?status=2&size=200";
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {});

            List<Map<String, Object>> instances = response.getBody();
            if (instances == null) return;

            for (Map<String, Object> instance : instances) {
                if (!isInProcess(instance)) continue;

                completedFound++;

                Long processInstanceId = ((Number) instance.get("id")).longValue();

                if (smsLogRepository.existsByProcessInstanceIdAndType(processInstanceId, "PORTAIN_SUCCESS")) continue;

                String msisdn = extractMsisdn(instance);
                if (msisdn == null) {
                    // Tentative de récupération des variables directement via l'API
                    try {
                        String varUrl = wsBaseUrl + "/api/monitoring/instances/" + processInstanceId + "/variables";
                        ResponseEntity<Map<String, Object>> varResp = restTemplate.exchange(
                                varUrl, HttpMethod.GET, null,
                                new ParameterizedTypeReference<Map<String, Object>>() {});
                        Map<String, Object> vars = varResp.getBody();
                        if (vars != null && !vars.isEmpty()) {
                            instance.put("variables", vars);
                            msisdn = extractMsisdn(instance);
                        }
                    } catch (Exception e) {
                        log.warn("Impossible de charger les variables via API pour l'instance {}: {}", processInstanceId, e.getMessage());
                    }
                }
                if (msisdn == null) {
                    log.warn("MSISDN introuvable pour l'instance {}, variables={}, containerId={}",
                            processInstanceId, instance.get("variables"), instance.get("containerId"));
                    continue;
                }

                String message = "F\u00e9licitations ! Votre num\u00e9ro " + msisdn
                        + " a \u00e9t\u00e9 port\u00e9 avec succ\u00e8s vers Orange Tunisie. Bienvenue chez Orange !";

                boolean sent = smsSenderService.sendSms(processInstanceId, msisdn, message);

                SmsLog smsLog = new SmsLog();
                smsLog.setMsisdn(msisdn);
                smsLog.setMessage(message);
                smsLog.setStatus(sent ? "SENT" : "FAILED");
                smsLog.setProcessInstanceId(processInstanceId);
                smsLog.setType("PORTAIN_SUCCESS");
                smsLog.setSentAt(LocalDateTime.now());

                smsLogRepository.save(smsLog);
                if (sent) sentCount++;

                // Envoyer SMS feedback après le SMS de succès
                if (!smsLogRepository.existsByProcessInstanceIdAndType(processInstanceId, "FEEDBACK_REQUEST")) {
                    String feedbackUrl = "http://localhost:3000/feedback/" + processInstanceId;
                    String feedbackMsg = "Donnez votre avis ! Comment s'est pass\u00e9e votre exp\u00e9rience de portabilit\u00e9 chez Orange ? "
                            + "Cliquez ici : " + feedbackUrl + " . Merci !";
                    boolean fbSent = smsSenderService.sendSms(processInstanceId, msisdn, feedbackMsg);

                    SmsLog fbLog = new SmsLog();
                    fbLog.setMsisdn(msisdn);
                    fbLog.setMessage(feedbackMsg);
                    fbLog.setStatus(fbSent ? "SENT" : "FAILED");
                    fbLog.setProcessInstanceId(processInstanceId);
                    fbLog.setType("FEEDBACK_REQUEST");
                    fbLog.setSentAt(LocalDateTime.now());
                    smsLogRepository.save(fbLog);
                    if (fbSent) sentCount++;
                }
            }
        } catch (Exception e) {
            log.error("Error in SMS scheduled task: {}", e.getMessage());
        }
    }

    private boolean isInProcess(Map<String, Object> instance) {
        Object containerId = instance.get("containerId");
        if (containerId != null && containerId.toString().toLowerCase().contains("out")) return false;

        @SuppressWarnings("unchecked")
        Map<String, Object> variables = (Map<String, Object>) instance.get("variables");
        if (variables != null && variables.containsKey("numeroOrange")) return true;

        return true;
    }

    private String extractMsisdn(Map<String, Object> instance) {
        @SuppressWarnings("unchecked")
        Map<String, Object> variables = (Map<String, Object>) instance.get("variables");

        if (variables != null) {
            Object msisdn = variables.get("msisdn");
            if (msisdn != null && !msisdn.toString().isEmpty()) return msisdn.toString();

            Object numeroOrange = variables.get("numeroOrange");
            if (numeroOrange != null && !numeroOrange.toString().isEmpty()) return numeroOrange.toString();

            Object phoneNumber = variables.get("phoneNumber");
            if (phoneNumber != null && !phoneNumber.toString().isEmpty()) return phoneNumber.toString();
        }

        Object idObj = instance.get("id");
        if (idObj != null) {
            Long pid;
            try { pid = Long.valueOf(idObj.toString()); } catch (NumberFormatException e) { return null; }
            PortabilityRequest pr = portabilityRequestRepository.findByProcessInstanceId(pid).orElse(null);
            if (pr != null) {
                if (pr.getMsisdn() != null && !pr.getMsisdn().isEmpty()) return pr.getMsisdn();
                if (pr.getNumeroOrange() != null && !pr.getNumeroOrange().isEmpty()) return pr.getNumeroOrange();
            }
        }

        return null;
    }

    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("lastRun", lastRun);
        status.put("sentCount", sentCount);
        status.put("completedFound", completedFound);
        return status;
    }
}
