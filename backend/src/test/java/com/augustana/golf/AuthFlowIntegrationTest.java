package com.augustana.golf;

import com.augustana.golf.domain.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.augustana.golf.repository.GamePlayerRepository;
import com.augustana.golf.repository.GameRepository;
import com.augustana.golf.repository.GolfCardRepository;
import com.augustana.golf.repository.RoundRepository;
import com.augustana.golf.repository.RoundScoreRepository;
import com.augustana.golf.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoundScoreRepository roundScoreRepository;

    @Autowired
    private GolfCardRepository golfCardRepository;

    @Autowired
    private RoundRepository roundRepository;

    @Autowired
    private GamePlayerRepository gamePlayerRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        roundScoreRepository.deleteAll();
        golfCardRepository.deleteAll();
        roundRepository.deleteAll();
        gamePlayerRepository.deleteAll();
        gameRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void signupThenLogin_withSameCredentials_succeeds() throws Exception {
        String signupRequestBody = """
                {
                  "username": "alice",
                  "password": "verysecure123",
                  "email": "alice@example.com"
                }
                """;

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupRequestBody))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.message").value("Signup successful."));

        Optional<User> savedUserOptional = userRepository.findByUsername("alice");
        assertTrue(savedUserOptional.isPresent());

        User savedUser = savedUserOptional.get();
        assertNotNull(savedUser.getUserId());
        assertEquals("alice", savedUser.getUsername());
        assertEquals("alice@example.com", savedUser.getEmail());
        assertNotEquals("verysecure123", savedUser.getPasswordHash());
        assertTrue(passwordEncoder.matches("verysecure123", savedUser.getPasswordHash()));

        String loginRequestBody = """
                {
                  "username": "alice",
                  "password": "verysecure123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(savedUser.getUserId()))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.message").value("Login successful."));
    }

    @Test
    void signup_duplicateUsername_returnsBadRequest() throws Exception {
        String signupRequestBody = """
                {
                  "username": "alice",
                  "password": "verysecure123",
                  "email": "alice@example.com"
                }
                """;

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupRequestBody))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupRequestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Username already exists."));
    }

    @Test
    void login_wrongPassword_returnsUnauthorized() throws Exception {
        String signupRequestBody = """
                {
                  "username": "alice",
                  "password": "verysecure123",
                  "email": "alice@example.com"
                }
                """;

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupRequestBody))
                .andExpect(status().isCreated());

        String loginRequestBody = """
                {
                  "username": "alice",
                  "password": "wrongpassword123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Invalid username or password."));
    }

    @Test
    void login_nonexistentUsername_returnsUnauthorized() throws Exception {
        String loginRequestBody = """
                {
                  "username": "ghost",
                  "password": "verysecure123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Invalid username or password."));
    }

    @Test
    void signup_blankEmail_savesNullEmail_andLoginStillWorks() throws Exception {
        String signupRequestBody = """
                {
                  "username": "alice",
                  "password": "verysecure123",
                  "email": "   "
                }
                """;

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupRequestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.message").value("Signup successful."));

        User savedUser = userRepository.findByUsername("alice").orElseThrow();
        assertNull(savedUser.getEmail());

        String loginRequestBody = """
                {
                  "username": "alice",
                  "password": "verysecure123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(savedUser.getUserId()))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.message").value("Login successful."));
    }

    @Test
    void signup_trimmedUsername_canLoginWithTrimmedUsernameLater() throws Exception {
        String signupRequestBody = """
                {
                  "username": "   alice   ",
                  "password": "verysecure123",
                  "email": "alice@example.com"
                }
                """;

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupRequestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.message").value("Signup successful."));

        User savedUser = userRepository.findByUsername("alice").orElseThrow();
        assertEquals("alice", savedUser.getUsername());

        String loginRequestBody = """
                {
                  "username": "   alice   ",
                  "password": "verysecure123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(savedUser.getUserId()))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.message").value("Login successful."));
    }

    @Test
    void signup_shortPassword_returnsBadRequest() throws Exception {
        String signupRequestBody = """
                {
                  "username": "alice",
                  "password": "short",
                  "email": "alice@example.com"
                }
                """;

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupRequestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Password must be at least 10 characters."));
    }

    @Test
    void login_sqlInjectionStyleUsername_returnsUnauthorized() throws Exception {
        String loginRequestBody = """
                {
                  "username": "' OR '1'='1",
                  "password": "anything"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Password must contain at least one number."));
    }

    @Test
    void createGame_withoutJwt_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "maxPlayers": 4
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Unauthorized"));
    }

    @Test
    void createGame_withJwt_returnsOk() throws Exception {
        String signupRequestBody = """
                {
                  "username": "alice",
                  "password": "verysecure123",
                  "email": "alice@example.com"
                }
                """;

        MvcResult signupResult = mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupRequestBody))
                .andExpect(status().isCreated())
                .andReturn();

        @SuppressWarnings("unchecked")
        Map<String, Object> signupResponse = objectMapper.readValue(
                signupResult.getResponse().getContentAsString(),
                Map.class
        );

        String token = (String) signupResponse.get("token");
        assertNotNull(token);
        assertFalse(token.isBlank());

        mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content("""
                                {
                                  "maxPlayers": 4
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").exists())
                .andExpect(jsonPath("$.gameCode").isString())
                .andExpect(jsonPath("$.players[0].username").value("alice"));
    }
}
