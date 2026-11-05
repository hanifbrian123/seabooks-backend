package com.fishcatch.backend.controller;

import com.fishcatch.backend.dto.AuthResponse;
import com.fishcatch.backend.dto.GoogleAuthRequest;
import com.fishcatch.backend.dto.LoginRequest;
import com.fishcatch.backend.dto.RegisterRequest;
import com.fishcatch.backend.model.User;
import com.fishcatch.backend.service.AuthService;
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

    @GetMapping("/me")
    public User getCurrentUser() {
        return authService.getCurrentUser();
    }
}
