package com.monitoring.controller;

import com.monitoring.entity.SmsLog;
import com.monitoring.repository.SmsLogRepository;
import com.monitoring.service.SmsScheduledService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sms")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SmsController {

    private static final Logger log = LoggerFactory.getLogger(SmsController.class);

    private final SmsLogRepository smsLogRepository;
    private final SmsScheduledService smsScheduledService;

    @PostMapping("/send")
    public ResponseEntity<SmsLog> sendSms(@RequestBody Map<String, Object> payload) {
        String msisdn = (String) payload.get("msisdn");
        String message = (String) payload.get("message");
        Long processInstanceId = payload.get("processInstanceId") != null
                ? Long.valueOf(payload.get("processInstanceId").toString()) : null;
        String type = (String) payload.get("type");

        log.info("Simulating SMS send to {}: {}", msisdn, message);

        SmsLog smsLog = new SmsLog();
        smsLog.setMsisdn(msisdn);
        smsLog.setMessage(message);
        smsLog.setStatus("SENT");
        smsLog.setProcessInstanceId(processInstanceId);
        smsLog.setType(type);

        SmsLog saved = smsLogRepository.save(smsLog);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/logs/{processInstanceId}")
    public ResponseEntity<List<SmsLog>> getLogsByProcessInstance(@PathVariable Long processInstanceId) {
        List<SmsLog> logs = smsLogRepository.findByProcessInstanceIdOrderBySentAtDesc(processInstanceId);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/auto-status")
    public ResponseEntity<Map<String, Object>> getAutoStatus() {
        return ResponseEntity.ok(smsScheduledService.getStatus());
    }
}
