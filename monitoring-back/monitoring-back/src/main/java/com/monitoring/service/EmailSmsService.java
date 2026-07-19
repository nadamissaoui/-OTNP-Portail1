package com.monitoring.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class EmailSmsService {

    private static final Logger log = LoggerFactory.getLogger(EmailSmsService.class);

    private static final Map<String, String> OPERATOR_DOMAINS = Map.of(
        "5", "orange.tn",
        "2", "ooredoo.tn",
        "9", "tt.tn"
    );

    private final JavaMailSender mailSender;
    private final String fromEmail;

    public EmailSmsService(JavaMailSender mailSender,
                           @Value("${sms.email.from:}") String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    public boolean sendSms(String to, String message) {
        String email = toEmail(to);
        if (email == null) {
            log.warn("Opérateur non supporté pour le numéro {}", to);
            return false;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromEmail);
            msg.setTo(email);
            msg.setSubject("Notification Orange Tunisie");
            msg.setText(message);
            mailSender.send(msg);
            log.info("SMS envoyé par email à {} (via {})", to, email);
            return true;
        } catch (Exception e) {
            log.error("Échec envoi email à {}: {}", email, e.getMessage());
            return false;
        }
    }

    private String toEmail(String msisdn) {
        if (msisdn == null || msisdn.length() < 3) return null;
        String num = msisdn.startsWith("+") ? msisdn.substring(1) : msisdn;
        num = num.startsWith("216") ? num : "216" + num.replaceAll("[^0-9]", "");
        String prefix = num.length() > 3 ? num.substring(3, 4) : "";
        String domain = OPERATOR_DOMAINS.get(prefix);
        if (domain == null) return null;
        return num + "@" + domain;
    }
}
