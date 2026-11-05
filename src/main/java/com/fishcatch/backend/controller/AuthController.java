package com.fishcatch.backend.controller;

import com.fishcatch.backend.dto.AuthResponse;
import com.fishcatch.backend.dto.GoogleAuthRequest;
import com.fishcatch.backend.dto.LoginRequest;
import com.fishcatch.backend.dto.RegisterRequest;
import com.fishcatch.backend.model.User;
import com.fishcatch.backend.service.AuthService;
import com.fishcatch.backend.service.DemoWorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final DemoWorkspaceService demoWorkspaceService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/google")
    public AuthResponse googleAuth(@RequestBody GoogleAuthRequest request) {
        return authService.authenticateWithGoogle(request);
    }

    @PostMapping("/demo")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse startDemoSession() {
        return demoWorkspaceService.createEphemeralDemoSession();
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout() {
        demoWorkspaceService.logoutAndCleanupIfDemo();
    }

    @GetMapping("/me")
    public User getCurrentUser() {
        return authService.getCurrentUser();
    }
}
