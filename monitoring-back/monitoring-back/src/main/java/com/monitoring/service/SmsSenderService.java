package com.monitoring.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class SmsSenderService {

    private static final Logger log = LoggerFactory.getLogger(SmsSenderService.class);
    private static final String TEXTBELT_URL = "https://textbelt.com/text";

    private final EmailSmsService emailSmsService;
    private final RestTemplate restTemplate;

    @Value("${sms.simulate:true}")
    private boolean simulate;

    public SmsSenderService(EmailSmsService emailSmsService, RestTemplate restTemplate) {
        this.emailSmsService = emailSmsService;
        this.restTemplate = restTemplate;
    }

    public boolean sendSms(Long processInstanceId, String to, String message) {
        if (simulate) {
            log.info("[SIMULATION] SMS envoyé à {}: {}", to, message);
            return true;
        }
        // Tentative 1: TextBelt (1 gratuit/jour)
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = Map.of(
                "phone", to,
                "message", message,
                "key", "textbelt"
            );

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(TEXTBELT_URL, request, Map.class);

            if (response.getBody() != null && Boolean.TRUE.equals(response.getBody().get("success"))) {
                log.info("SMS envoyé via TextBelt à {}", to);
                return true;
            }
        } catch (Exception e) {
            log.warn("TextBelt échoué pour {}: {}", to, e.getMessage());
        }

        // Tentative 2: email-to-SMS via opérateur
        log.info("Fallback email-to-SMS pour {}", to);
        return emailSmsService.sendSms(to, message);
    }
}
