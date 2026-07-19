package tn.esprit.smsservice.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.smsservice.dto.SmsRequest;

@RestController
@RequestMapping("/bscs/sms")
public class SmsController {

    @PostMapping("/send")
    public ResponseEntity<String> sendSms(@RequestBody SmsRequest request) {
        String phone = request.getPhoneNumber();

        // 1. LOGIQUE MÉTIER ORANGE TUNISIE (Validation manuelle)
        if (phone == null || phone.length() != 8 || !phone.startsWith("5")) {
            System.err.println("!!! ÉCHEC : Numéro non-Orange (" + phone + ") !!!");
            return ResponseEntity.badRequest()
                    .body("ERREUR : Le numéro doit avoir 8 chiffres et commencer par 5.");
        }

        // 2. LOGS DE RÉCEPTION (Pour ta démonstration)
        System.out.println("----------------------------------------------");
        System.out.println("DEMANDE SMS ORANGE VALIDE");
        System.out.println("Destinataire : " + phone);
        System.out.println("Message : " + request.getMessage());
        System.out.println("----------------------------------------------");

        return ResponseEntity.ok("SENT AVEC SUCCÈS");
    }
}