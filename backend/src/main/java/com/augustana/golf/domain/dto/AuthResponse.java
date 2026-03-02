package com.augustana.golf.domain.dto;

public record AuthResponse(Long userId, String username, String email, String message) {
}
