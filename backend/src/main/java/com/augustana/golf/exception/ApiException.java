package com.augustana.golf.exception;

import org.springframework.http.HttpStatus;

/**
 * Runtime exception for expected API failures that need a specific HTTP status.
 */
public class ApiException extends RuntimeException {
    private final HttpStatus status;
    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
    public HttpStatus getStatus() { 
        return status; 
    }
}
