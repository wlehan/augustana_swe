package com.augustana.golf.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Authentication response returned after signup or login.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(Long userId, String username, String token, String message) {
}
