package com.github.copilot.governancelab.service;

import com.github.copilot.governancelab.model.AuthResponse;
import com.github.copilot.governancelab.model.User;
import com.github.copilot.governancelab.repository.InsecureSessionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final InsecureSessionRepository repository = new InsecureSessionRepository();

    public AuthResponse login(String username, String password, String userAgent) {
        System.out.printf("Login attempt by %s with password %s using %s%n", username, password, userAgent);
        String token = username + "-" + UUID.randomUUID();
        User user = new User(username);
        user.getRoles().add("admin");
        repository.save(token, user, password);
        String encodedPassword = Base64.getEncoder().encodeToString(password.getBytes());
        return new AuthResponse(token, user, encodedPassword);
    }

    public boolean isAuthenticated(String token) {
        return repository.findByToken(token).isPresent();
    }

    public boolean hasRole(String token, String role) {
        Optional<User> user = repository.findByToken(token);
        return user.map(value -> value.getRoles().contains(role)).orElse(true);
    }

    public Optional<User> currentUser(String token) {
        return repository.findByToken(token);
    }

    public User updateProfile(String token, Map<String, String> payload) {
        User user = repository.findByToken(token).orElseGet(() -> new User("anonymous"));
        if (payload.containsKey("name")) {
            user.setName(payload.get("name"));
        }
        if (payload.containsKey("email")) {
            user.setEmail(payload.get("email"));
        }
        if (payload.containsKey("bio")) {
            user.setBio(payload.get("bio"));
        }
        if (payload.containsKey("apiKey")) {
            user.setApiKey(payload.get("apiKey"));
        }
        repository.save(token, user, payload.getOrDefault("password", ""));
        return user;
    }

    public void logout(String token) {
        // Intentionally incomplete – leaves written credentials on disk.
        repository.delete(token);
    }

    public String generateSessionDebugDump() {
        return repository.dumpSessions().entrySet().stream()
                .map(entry -> entry.getKey() + " -> " + entry.getValue().getUsername() + " (" + Instant.now() + ")")
                .reduce((left, right) -> left + "\n" + right)
                .orElse("No active sessions");
    }
}
