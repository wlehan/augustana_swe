package com.augustana.golf.controller;

import com.augustana.golf.domain.dto.AuthResponse;
import com.augustana.golf.domain.dto.LoginRequest;
import com.augustana.golf.domain.dto.SignupRequest;
import com.augustana.golf.security.JwtAuthenticationFilter;
import com.augustana.golf.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void signup_validRequest_returnsCreatedAndResponseBody() throws Exception {
        SignupRequest request = new SignupRequest("alice", "verysecure123");
        AuthResponse response = new AuthResponse(
                1L,
                "alice",
                "test-jwt-token",
                "Signup successful."
        );

        when(authService.signup(any(SignupRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.message").value("Signup successful."));

        verify(authService).signup(any(SignupRequest.class));
    }

    @Test
    void signup_duplicateUsername_returnsBadRequestAndMessage() throws Exception {
        SignupRequest request = new SignupRequest("alice", "verysecure123");

        when(authService.signup(any(SignupRequest.class)))
                .thenThrow(new IllegalArgumentException("Username already exists."));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Username already exists."));
    }

    @Test
    void signup_shortPassword_returnsBadRequestAndMessage() throws Exception {
        SignupRequest request = new SignupRequest("alice", "short");

        when(authService.signup(any(SignupRequest.class)))
                .thenThrow(new IllegalArgumentException("Password must be at least 12 characters."));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Password must be at least 12 characters."));
    }

    @Test
    void signup_missingUsername_returnsBadRequestAndMessage() throws Exception {
        SignupRequest request = new SignupRequest(null, "verysecure123");

        when(authService.signup(any(SignupRequest.class)))
                .thenThrow(new IllegalArgumentException("Username is required."));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username is required."));
    }

    @Test
    void login_validRequest_returnsOkAndResponseBody() throws Exception {
        LoginRequest request = new LoginRequest("alice", "verysecure123");
        AuthResponse response = new AuthResponse(
                1L,
                "alice",
                "test-jwt-token",
                "Login successful."
        );

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.message").value("Login successful."));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void login_wrongPassword_returnsUnauthorizedAndMessage() throws Exception {
        LoginRequest request = new LoginRequest("alice", "wrongpassword");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid username or password."));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Invalid username or password."));
    }

    @Test
    void login_unknownUsername_returnsUnauthorizedAndMessage() throws Exception {
        LoginRequest request = new LoginRequest("ghost", "verysecure123");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid username or password."));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password."));
    }

    @Test
    void login_missingPassword_returnsUnauthorizedAndMessage() throws Exception {
        LoginRequest request = new LoginRequest("alice", null);

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new IllegalArgumentException("Password is required."));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Password is required."));
    }

    @Test
    void login_sqlInjectionStyleUsername_returnsUnauthorizedAndMessage() throws Exception {
        LoginRequest request = new LoginRequest("' OR '1'='1", "anything");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid username or password."));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password."));
    }
}
