package com.augustana.golf.domain.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthResponseTest {

    @Test
    void authResponse_record_hasAllFields() {
        AuthResponse response = new AuthResponse(100L, "testuser", "test@example.com", "Login successful");
        
        assertEquals(100L, response.userId());
        assertEquals("testuser", response.username());
        assertEquals("test@example.com", response.email());
        assertEquals("Login successful", response.message());
    }

    @Test
    void authResponse_withNullMessage() {
        AuthResponse response = new AuthResponse(100L, "testuser", "test@example.com", null);
        
        assertNull(response.message());
    }

    @Test
    void authResponse_multipleInstances_areEqual() {
        AuthResponse response1 = new AuthResponse(100L, "testuser", "test@example.com", "Success");
        AuthResponse response2 = new AuthResponse(100L, "testuser", "test@example.com", "Success");
        
        assertEquals(response1, response2);
    }

    @Test
    void authResponse_toString_works() {
        AuthResponse response = new AuthResponse(100L, "testuser", "test@example.com", "Success");
        String str = response.toString();
        
        assertNotNull(str);
        assertTrue(str.contains("testuser"));
    }
}
