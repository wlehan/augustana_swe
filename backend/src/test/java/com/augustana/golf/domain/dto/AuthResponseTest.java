package com.augustana.golf.domain.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class AuthResponseTest {

    @Test
    void authResponse_record_hasAllFields() {
        AuthResponse response = new AuthResponse(100L, "testuser", "test@example.com", "fake-token", "Login successful");
        
        assertEquals(100L, response.userId());
        assertEquals("testuser", response.username());
        assertEquals("test@example.com", response.email());
        assertEquals("Login successful", response.message());
    }

    @Test
    void authResponse_withNullMessage() {
        AuthResponse response = new AuthResponse(100L, "testuser", "test@example.com", "fake-token", null);
        
        assertNull(response.message());
    }

    @Test
    void authResponse_multipleInstances_areEqual() {
        AuthResponse response1 = new AuthResponse(100L, "testuser", "test@example.com", "fake-token", "Success");
        AuthResponse response2 = new AuthResponse(100L, "testuser", "test@example.com", "fake-token", "Success");
        
        assertEquals(response1, response2);
    }

    @Test
    void authResponse_toString_works() {
        AuthResponse response = new AuthResponse(100L, "testuser", "test@example.com", "fake-token", "Success");
        String str = response.toString();
        
        assertNotNull(str);
        assertTrue(str.contains("testuser"));
    }
}
