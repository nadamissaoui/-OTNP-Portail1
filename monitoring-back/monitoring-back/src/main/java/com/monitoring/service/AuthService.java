package com.monitoring.service;

import com.monitoring.model.Agent;
import com.monitoring.repository.AgentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AgentRepository agentRepository;

    public boolean usernameExists(String username) {
        return agentRepository.existsByUsername(username);
    }

    public Optional<Agent> login(String username, String rawPassword) {
        System.out.println(">>> Login: " + username + " / " + rawPassword);
        return agentRepository.findByUsername(username)
                .filter(agent -> {
                    System.out.println(">>> Password en base : " + agent.getPassword());
                    boolean match = agent.getPassword().equals(rawPassword.trim());
                    System.out.println(">>> Match : " + match);
                    return match;
                });
    }
}
