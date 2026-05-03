package com.augustana.golf.exception;

/**
 * Legacy not-found exception handled by {@link ApiExceptionHandler}.
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
