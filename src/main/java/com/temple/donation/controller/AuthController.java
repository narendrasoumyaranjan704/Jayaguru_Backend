package com.temple.donation.controller;

import com.temple.donation.dto.LoginRequest;
import com.temple.donation.dto.LoginResponse;
import com.temple.donation.dto.MessageResponse;
import com.temple.donation.dto.UserResponse;
import com.temple.donation.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    public MessageResponse logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return MessageResponse.of("Logged out");
    }

    @GetMapping("/me")
    public UserResponse me(@RequestHeader("Authorization") String authorization) {
        String token = authorization.substring("Bearer ".length());
        return authService.currentUser(token);
    }
}
