package com.augustana.golf.domain.dto;

/**
 * Login credentials submitted by the React login form.
 */
public record LoginRequest(String username, String password) {
}
