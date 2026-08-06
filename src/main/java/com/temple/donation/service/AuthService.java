package com.temple.donation.service;

import com.temple.donation.dto.LoginRequest;
import com.temple.donation.dto.LoginResponse;
import com.temple.donation.dto.UserResponse;
import com.temple.donation.entity.AppUser;
import com.temple.donation.exception.UnauthorizedException;
import com.temple.donation.repository.AppUserRepository;
import com.temple.donation.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AppUserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        AppUser user = userRepository.findByUsernameIgnoreCase(request.username().trim())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("Invalid username or password");
        }
        String token = jwtService.generateToken(user.getUsername());
        return new LoginResponse(token, user.getUsername(), user.getDisplayName());
    }

    public UserResponse currentUser(String token) {
        String username = jwtService.parseToken(token);
        AppUser user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        return new UserResponse(user.getUsername(), user.getDisplayName());
    }
}
