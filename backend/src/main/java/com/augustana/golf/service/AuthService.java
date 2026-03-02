package com.augustana.golf.service;

import com.augustana.golf.domain.dto.AuthResponse;
import com.augustana.golf.domain.dto.LoginRequest;
import com.augustana.golf.domain.dto.SignupRequest;
import com.augustana.golf.domain.model.User;
import com.augustana.golf.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse signup(SignupRequest request) {
        String username = normalizeUsername(request.username());
        String password = normalizePassword(request.password());
        String email = normalizeEmail(request.email());

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists.");
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setEmail(email);

        User saved = userRepository.save(user);

        return new AuthResponse(saved.getUserId(), saved.getUsername(), saved.getEmail(), "Signup successful.");
    }

    public AuthResponse login(LoginRequest request) {
        String username = normalizeUsername(request.username());
        String password = normalizePassword(request.password());

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password."));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password.");
        }

        return new AuthResponse(user.getUserId(), user.getUsername(), user.getEmail(), "Login successful.");
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

    private String normalizePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }

        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }

        return password;
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }

        String cleaned = email.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }
}
