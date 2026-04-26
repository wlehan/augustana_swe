package com.augustana.golf.service;

import com.augustana.golf.domain.dto.AuthResponse;
import com.augustana.golf.domain.dto.LoginRequest;
import com.augustana.golf.domain.dto.SignupRequest;
import com.augustana.golf.domain.model.User;
import com.augustana.golf.repository.UserRepository;
import com.augustana.golf.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void signup_successfulRequest_returnsAuthResponse() {
        SignupRequest request = new SignupRequest("alice", "verysecure123", "alice@example.com");

        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(passwordEncoder.encode("verysecure123")).thenReturn("hashed-password");
        when(jwtService.generateToken(any(User.class))).thenReturn("test-jwt-token");

        User savedUser = new User();
        savedUser.setUserId(1L);
        savedUser.setUsername("alice");
        savedUser.setPasswordHash("hashed-password");
        savedUser.setEmail("alice@example.com");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        AuthResponse response = authService.signup(request);

        assertEquals(1L, response.userId());
        assertEquals("alice", response.username());
        assertEquals("alice@example.com", response.email());
        assertEquals("test-jwt-token", response.token());
        assertEquals("Signup successful.", response.message());
    }

    @Test
    void signup_trimsUsernameBeforeCheckingAndSaving() {
        SignupRequest request = new SignupRequest("  alice  ", "verysecure123", "alice@example.com");

        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(passwordEncoder.encode("verysecure123")).thenReturn("hashed-password");
        when(jwtService.generateToken(any(User.class))).thenReturn("test-jwt-token");

        User savedUser = new User();
        savedUser.setUserId(1L);
        savedUser.setUsername("alice");
        savedUser.setPasswordHash("hashed-password");
        savedUser.setEmail("alice@example.com");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        authService.signup(request);

        verify(userRepository).existsByUsername("alice");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User userToSave = userCaptor.getValue();
        assertEquals("alice", userToSave.getUsername());
    }

    @Test
    void signup_blankEmail_becomesNull() {
        SignupRequest request = new SignupRequest("alice", "verysecure123", "   ");

        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(passwordEncoder.encode("verysecure123")).thenReturn("hashed-password");
        when(jwtService.generateToken(any(User.class))).thenReturn("test-jwt-token");

        User savedUser = new User();
        savedUser.setUserId(1L);
        savedUser.setUsername("alice");
        savedUser.setPasswordHash("hashed-password");
        savedUser.setEmail(null);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        AuthResponse response = authService.signup(request);

        assertNull(response.email());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User userToSave = userCaptor.getValue();
        assertNull(userToSave.getEmail());
    }

    @Test
    void signup_nullUsername_throwsException() {
        SignupRequest request = new SignupRequest(null, "verysecure123", "alice@example.com");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.signup(request)
        );

        assertEquals("Username is required.", exception.getMessage());
        verify(userRepository, never()).existsByUsername(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void signup_blankUsername_throwsException() {
        SignupRequest request = new SignupRequest("   ", "verysecure123", "alice@example.com");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.signup(request)
        );

        assertEquals("Username is required.", exception.getMessage());
        verify(userRepository, never()).existsByUsername(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void signup_nullPassword_throwsException() {
        SignupRequest request = new SignupRequest("alice", null, "alice@example.com");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.signup(request)
        );

        assertEquals("Password is required.", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void signup_blankPassword_throwsException() {
        SignupRequest request = new SignupRequest("alice", "   ", "alice@example.com");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.signup(request)
        );

        assertEquals("Password is required.", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void signup_shortPassword_throwsException() {
        SignupRequest request = new SignupRequest("alice", "short", "alice@example.com");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.signup(request)
        );

        assertEquals("Password must be at least 10 characters.", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void signup_duplicateUsername_throwsException() {
        SignupRequest request = new SignupRequest("alice", "verysecure123", "alice@example.com");

        when(userRepository.existsByUsername("alice")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.signup(request)
        );

        assertEquals("Username already exists.", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void signup_hashesPasswordBeforeSaving() {
        SignupRequest request = new SignupRequest("alice", "verysecure123", "alice@example.com");

        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(passwordEncoder.encode("verysecure123")).thenReturn("hashed-password");
        when(jwtService.generateToken(any(User.class))).thenReturn("test-jwt-token");

        User savedUser = new User();
        savedUser.setUserId(1L);
        savedUser.setUsername("alice");
        savedUser.setPasswordHash("hashed-password");
        savedUser.setEmail("alice@example.com");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        authService.signup(request);

        verify(passwordEncoder).encode("verysecure123");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User userToSave = userCaptor.getValue();
        assertEquals("hashed-password", userToSave.getPasswordHash());
        assertNotEquals("verysecure123", userToSave.getPasswordHash());
    }

    @Test
    void login_successfulRequest_returnsAuthResponse() {
        LoginRequest request = new LoginRequest("alice", "verysecure123");

        User user = new User();
        user.setUserId(1L);
        user.setUsername("alice");
        user.setPasswordHash("hashed-password");
        user.setEmail("alice@example.com");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("verysecure123", "hashed-password")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("test-jwt-token");

        AuthResponse response = authService.login(request);

        assertEquals(1L, response.userId());
        assertEquals("alice", response.username());
        assertEquals("alice@example.com", response.email());
        assertEquals("test-jwt-token", response.token());
        assertEquals("Login successful.", response.message());
    }

    @Test
    void login_trimsUsernameBeforeLookup() {
        LoginRequest request = new LoginRequest("  alice  ", "verysecure123");

        User user = new User();
        user.setUserId(1L);
        user.setUsername("alice");
        user.setPasswordHash("hashed-password");
        user.setEmail("alice@example.com");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("verysecure123", "hashed-password")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("test-jwt-token");

        authService.login(request);

        verify(userRepository).findByUsername("alice");
    }

    @Test
    void login_nullUsername_throwsException() {
        LoginRequest request = new LoginRequest(null, "verysecure123");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(request)
        );

        assertEquals("Username is required.", exception.getMessage());
        verify(userRepository, never()).findByUsername(any());
    }

    @Test
    void login_blankUsername_throwsException() {
        LoginRequest request = new LoginRequest("   ", "verysecure123");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(request)
        );

        assertEquals("Username is required.", exception.getMessage());
        verify(userRepository, never()).findByUsername(any());
    }

    @Test
    void login_nullPassword_throwsException() {
        LoginRequest request = new LoginRequest("alice", null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(request)
        );

        assertEquals("Password is required.", exception.getMessage());
        verify(userRepository, never()).findByUsername(any());
    }

    @Test
    void login_blankPassword_throwsException() {
        LoginRequest request = new LoginRequest("alice", "   ");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(request)
        );

        assertEquals("Password is required.", exception.getMessage());
        verify(userRepository, never()).findByUsername(any());
    }

    @Test
    void login_unknownUsername_throwsException() {
        LoginRequest request = new LoginRequest("alice", "verysecure123");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(request)
        );

        assertEquals("Invalid username or password.", exception.getMessage());
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void login_wrongPassword_throwsException() {
        LoginRequest request = new LoginRequest("alice", "wrongpassword1");

        User user = new User();
        user.setUserId(1L);
        user.setUsername("alice");
        user.setPasswordHash("hashed-password1");
        user.setEmail("alice@example.com");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword1", "hashed-password1")).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(request)
        );

        assertEquals("Invalid username or password.", exception.getMessage());
    }
}
