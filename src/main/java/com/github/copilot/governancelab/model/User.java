package com.github.copilot.governancelab.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class User {
    private String username;
    private String name;
    private String email;
    private String apiKey = UUID.randomUUID().toString();
    private String sessionId = UUID.randomUUID() + "-" + Instant.now().toEpochMilli();
    private String bio = "<p>Welcome back! Paste anything here – we trust you.</p>";
    private List<String> roles = new ArrayList<>();

    public User() {
    }

    public User(String username) {
        this.username = username;
        this.name = username;
        this.email = username + "@example.com";
        this.roles.add("user");
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
