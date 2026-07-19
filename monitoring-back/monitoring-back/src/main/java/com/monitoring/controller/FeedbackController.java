package com.monitoring.controller;

import com.monitoring.entity.Feedback;
import com.monitoring.repository.FeedbackRepository;
import com.monitoring.entity.SmsLog;
import com.monitoring.repository.SmsLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackRepository feedbackRepository;
    private final SmsLogRepository smsLogRepository;

    @PostMapping("/submit")
    public ResponseEntity<?> submitFeedback(@RequestBody Map<String, Object> payload) {
        Long processInstanceId = Long.valueOf(payload.get("processInstanceId").toString());
        String msisdn = (String) payload.get("msisdn");
        Integer rating = Integer.valueOf(payload.get("rating").toString());
        String comment = (String) payload.getOrDefault("comment", "");
        String source = (String) payload.getOrDefault("source", "agent");

        if (feedbackRepository.existsByProcessInstanceId(processInstanceId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Feedback déjà soumis pour ce processus"));
        }

        Feedback feedback = new Feedback();
        feedback.setProcessInstanceId(processInstanceId);
        feedback.setMsisdn(msisdn);
        feedback.setRating(rating);
        feedback.setComment(comment);
        feedback.setSource(source);

        Feedback saved = feedbackRepository.save(feedback);

        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{processInstanceId}")
    public ResponseEntity<?> getFeedback(@PathVariable Long processInstanceId) {
        Optional<Feedback> feedback = feedbackRepository.findByProcessInstanceId(processInstanceId);
        if (feedback.isPresent()) {
            return ResponseEntity.ok(feedback.get());
        }
        return ResponseEntity.ok(Map.of("exists", false));
    }
}
