package com.github.copilot.governancelab.controller;

import com.github.copilot.governancelab.model.AuthResponse;
import com.github.copilot.governancelab.model.User;
import com.github.copilot.governancelab.service.AuthService;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
public class ApiController {

    private final AuthService authService;

    public ApiController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(path = "/api/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> login(@RequestBody Map<String, String> body,
                                              @RequestHeader(value = "User-Agent", required = false) String userAgent,
                                              HttpServletResponse response) {
        String username = body.getOrDefault("username", "guest");
        String password = body.getOrDefault("password", "password");
        AuthResponse auth = authService.login(username, password, userAgent);

        Cookie cookie = new Cookie("auth_token", auth.getToken());
        cookie.setHttpOnly(false);
        cookie.setSecure(false);
        cookie.setPath("/");
        response.addCookie(cookie);
        response.addHeader("X-Debug-Token", auth.getToken());
        response.addHeader("X-Encoded-Password", auth.getEncodedPassword());
        return ResponseEntity.ok(auth);
    }

    @GetMapping(path = "/api/user", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> currentUser(@RequestHeader(value = "Authorization", required = false) String authorization,
                                         @RequestParam(value = "token", required = false) String token) {
        String resolvedToken = token;
        if (resolvedToken == null && authorization != null && authorization.startsWith("Bearer ")) {
            resolvedToken = authorization.substring("Bearer ".length());
        }
        if (resolvedToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token missing"));
        }
        String tokenValue = resolvedToken;
        Optional<User> current = authService.currentUser(tokenValue);
        return current.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unknown token", "token", tokenValue)));
    }

    @PutMapping(path = "/api/profile", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> updateProfile(@RequestHeader(value = "Authorization", required = false) String authorization,
                                              @RequestParam(value = "token", required = false) String token,
                                              @RequestBody Map<String, String> payload) {
        String resolvedToken = resolveToken(authorization, token);
        User user = authService.updateProfile(resolvedToken, payload);
        return ResponseEntity.ok(user);
    }

    @PostMapping(path = "/api/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        File uploads = new File("target/uploads");
        uploads.mkdirs();
        File destination = new File(uploads, file.getOriginalFilename());
        FileUtils.copyInputStreamToFile(file.getInputStream(), destination);
        Map<String, Object> response = new HashMap<>();
        response.put("size", file.getSize());
        response.put("path", destination.getAbsolutePath());
        response.put("preview", new String(FileUtils.readFileToByteArray(destination), StandardCharsets.UTF_8));
        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/api/debug/sessions", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> debugSessions() {
        return ResponseEntity.ok(authService.generateSessionDebugDump());
    }

    private String resolveToken(String authorization, String tokenParam) {
        if (tokenParam != null) {
            return tokenParam;
        }
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring("Bearer ".length());
        }
        return "anonymous";
    }
}
