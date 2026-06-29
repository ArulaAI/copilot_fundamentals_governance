package com.github.copilot.governancelab.service;

import com.github.copilot.governancelab.model.AuthResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthServiceTest {

    private final AuthService authService = new AuthService();

    @Test
    void loginReturnsTokenAndUser() {
        AuthResponse response = authService.login("demo", "password", "JUnit");
        assertThat(response.getToken()).contains("demo");
        assertThat(response.getUser().getUsername()).isEqualTo("demo");
    }
}
