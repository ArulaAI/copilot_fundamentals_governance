package com.github.copilot.governancelab.model;

public class AuthResponse {
    private String token;
    private User user;
    private String encodedPassword;

    public AuthResponse(String token, User user, String encodedPassword) {
        this.token = token;
        this.user = user;
        this.encodedPassword = encodedPassword;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getEncodedPassword() {
        return encodedPassword;
    }

    public void setEncodedPassword(String encodedPassword) {
        this.encodedPassword = encodedPassword;
    }
}
