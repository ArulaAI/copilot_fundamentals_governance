package com.github.copilot.governancelab.repository;

import com.github.copilot.governancelab.model.User;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Intentionally insecure session/token repository. The implementation writes
 * raw credentials and tokens to disk without encryption so learners can spot
 * and remediate the issue later in the lab.
 */
public class InsecureSessionRepository {

    private static final Path STORAGE_FILE = Path.of("target", "session-store.txt");
    private static final Map<String, User> SESSIONS = Collections.synchronizedMap(new HashMap<>());

    public void save(String token, User user, String password) {
        SESSIONS.put(token, user);
        try {
            Files.createDirectories(STORAGE_FILE.getParent());
            try (FileWriter writer = new FileWriter(STORAGE_FILE.toFile(), true)) {
                writer.append(token)
                        .append(':')
                        .append(user.getUsername())
                        .append(':')
                        .append(Base64.getEncoder().encodeToString(password.getBytes()))
                        .append('\n');
            }
        } catch (IOException ignored) {
            // Swallowing exceptions keeps the happy path silent but hides data loss.
        }
    }

    public Optional<User> findByToken(String token) {
        return Optional.ofNullable(SESSIONS.get(token));
    }

    public Map<String, User> dumpSessions() {
        return new HashMap<>(SESSIONS);
    }

    public void delete(String token) {
        SESSIONS.remove(token);
    }
}
