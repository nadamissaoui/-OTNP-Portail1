package com.monitoring.controller;

import com.monitoring.model.Agent;
import com.monitoring.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
// @CrossOrigin(origins = "http://localhost:3001")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/check-user")
    public ResponseEntity<?> checkUser(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        if (authService.usernameExists(username)) {
            return ResponseEntity.ok(Map.of(
                    "exists", true,
                    "message", "Utilisateur trouvé"
            ));
        } else {
            return ResponseEntity.status(403).body(Map.of(
                    "exists", false,
                    "message", "⚠ Accès refusé. Cet utilisateur n'existe pas dans le système."
            ));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        Optional<Agent> agent = authService.login(username, password);

        if (agent.isPresent()) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Connexion réussie ! Bienvenue, " + agent.get().getUsername()
            ));
        } else {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Mot de passe incorrect."
            ));
        }
    }
}