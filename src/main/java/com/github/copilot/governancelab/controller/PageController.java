package com.github.copilot.governancelab.controller;

import com.github.copilot.governancelab.model.AuthResponse;
import com.github.copilot.governancelab.model.User;
import com.github.copilot.governancelab.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PageController {

    private final AuthService authService;

    public PageController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping({"/", "/login"})
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "returnUrl", required = false) String returnUrl,
                            Model model) {
        model.addAttribute("errorMessage", error);
        model.addAttribute("returnUrl", returnUrl);
        model.addAttribute("debugMode", true);
        model.addAttribute("systemInfo", buildSystemInfo());
        return "login";
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam String username,
                              @RequestParam String password,
                              @RequestParam(value = "returnUrl", required = false) String returnUrl,
                              HttpServletRequest request,
                              Model model) {
        AuthResponse response = authService.login(username, password, request.getHeader("User-Agent"));
        HttpSession session = request.getSession(true);
        session.setAttribute("token", response.getToken());
        session.setAttribute("user", response.getUser());
        session.setAttribute("encodedPassword", response.getEncodedPassword());
        session.setAttribute("plainPassword", password);
        session.setAttribute("loginTimestamp", Instant.now().toString());

        if (StringUtils.hasText(returnUrl)) {
            return "redirect:" + returnUrl;
        }

        model.addAttribute("token", response.getToken());
        model.addAttribute("user", response.getUser());
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login?error=No+active+session";
        }
        model.addAttribute("user", user);
        model.addAttribute("token", session.getAttribute("token"));
        model.addAttribute("encodedPassword", session.getAttribute("encodedPassword"));
        model.addAttribute("plainPassword", session.getAttribute("plainPassword"));
        model.addAttribute("loginTimestamp", session.getAttribute("loginTimestamp"));
        return "dashboard";
    }

    @PostMapping("/profile")
    public String updateProfile(HttpSession session,
                                @RequestParam Map<String, String> payload) {
        String token = (String) session.getAttribute("token");
        authService.updateProfile(token, payload);
        return "redirect:/dashboard";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        String token = (String) session.getAttribute("token");
        authService.logout(token);
        session.invalidate();
        return "redirect:/login?error=Logged+out";
    }

    private Map<String, Object> buildSystemInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("version", "1.0.0");
        try {
            info.put("server", InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            info.put("server", "unknown-host");
        }
        info.put("java", System.getProperty("java.version"));
        info.put("os", System.getProperty("os.name"));
        return info;
    }
}
