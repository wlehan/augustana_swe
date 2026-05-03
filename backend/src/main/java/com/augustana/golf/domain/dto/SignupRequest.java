package com.augustana.golf.domain.dto;

/**
 * Signup credentials submitted by the React account creation form.
 */
public record SignupRequest(String username, String password) {
}
