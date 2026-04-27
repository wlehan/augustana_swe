package com.augustana.golf.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.augustana.golf.domain.dto.AuthResponse;
import com.augustana.golf.domain.dto.LoginRequest;
import com.augustana.golf.domain.dto.SignupRequest;
import com.augustana.golf.domain.model.User;
import com.augustana.golf.repository.UserRepository;
import com.augustana.golf.security.JwtService;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse signup(SignupRequest request) {
        String username = normalizeUsername(request.username());
        String password = normalizePassword(request.password(), true);

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists.");
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));

        User saved = userRepository.save(user);
        String token = jwtService.generateToken(saved);

        return new AuthResponse(
                saved.getUserId(),
                saved.getUsername(),
                token,
                "Signup successful."
        );
    }

    public AuthResponse login(LoginRequest request) {
        String username = normalizeUsername(request.username());
        String password = normalizePassword(request.password(), false);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password."));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password.");
        }

        String token = jwtService.generateToken(user);

        return new AuthResponse(
                user.getUserId(),
                user.getUsername(),
                token,
                "Login successful."
        );
    }

    private String normalizeUsername(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Username is required.");
        }

        String cleaned = username.trim();
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }

        return cleaned;
    }

    private String normalizePassword(String password, boolean enforceMinLength) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }
        boolean hasNumber = false;

        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) {
                hasNumber = true;
            }
        }

        if (enforceMinLength && password.length() < 10) {
            throw new IllegalArgumentException("Password must be at least 10 characters.");
        }
        if (!hasNumber) {
            throw new IllegalArgumentException("Password must contain at least one number.");
        }

        return password;
    }
}
