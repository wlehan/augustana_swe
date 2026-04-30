package com.augustana.golf.controller;

import com.augustana.golf.domain.dto.CreateGameRequest;
import com.augustana.golf.domain.dto.DiscardCardRequest;
import com.augustana.golf.domain.dto.DrawCardRequest;
import com.augustana.golf.domain.dto.FlipInitialRequest;
import com.augustana.golf.domain.dto.GameResponse;
import com.augustana.golf.domain.dto.GameStateResponse;
import com.augustana.golf.domain.dto.JoinGameRequest;
import com.augustana.golf.domain.dto.SwapCardRequest;
import com.augustana.golf.security.JwtAuthenticationFilter;
import com.augustana.golf.service.GameActionService;
import com.augustana.golf.service.GameService;
import com.augustana.golf.service.RoundService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GameController.class)
@AutoConfigureMockMvc(addFilters = false)
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GameService gameService;

    @MockitoBean
    private RoundService roundService;

    @MockitoBean
    private GameActionService gameActionService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void createGame_noRequestBody_createsGameWithDefaultMaxPlayers() throws Exception {
        GameResponse gameResponse = new GameResponse();
        setField(gameResponse, "gameId", 1L);
        gameResponse.setGameCode("ABC123");
        gameResponse.setStatus("WAITING");
        gameResponse.setMaxPlayers(4);

        when(gameService.createGame(1L, 4)).thenReturn(gameResponse);

        mockMvc.perform(post("/api/games")
                .header("X-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.gameId").value(1))
                .andExpect(jsonPath("$.gameCode").value("ABC123"))
                .andExpect(jsonPath("$.maxPlayers").value(4));

        verify(gameService).createGame(1L, 4);
    }

    @Test
    void createGame_withRequestBody_createsGameWithSpecifiedMaxPlayers() throws Exception {
        CreateGameRequest request = new CreateGameRequest();
        setField(request, "maxPlayers", 2);

        GameResponse gameResponse = new GameResponse();
        setField(gameResponse, "gameId", 1L);
        gameResponse.setGameCode("ABC123");
        gameResponse.setStatus("WAITING");
        gameResponse.setMaxPlayers(2);

        when(gameService.createGame(1L, 2)).thenReturn(gameResponse);

        mockMvc.perform(post("/api/games")
                .header("X-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxPlayers").value(2));

        verify(gameService).createGame(1L, 2);
    }

    @Test
    void joinGame_validRequest_joinsGameByCode() throws Exception {
        JoinGameRequest request = new JoinGameRequest();
        setField(request, "gameCode", "ABC123");

        GameResponse gameResponse = new GameResponse();
        setField(gameResponse, "gameId", 1L);
        gameResponse.setGameCode("ABC123");
        gameResponse.setStatus("WAITING");
        gameResponse.setMaxPlayers(4);

        when(gameService.joinGameByCode(2L, "ABC123")).thenReturn(gameResponse);

        mockMvc.perform(post("/api/games/join")
                .header("X-User-Id", "2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameCode").value("ABC123"));

        verify(gameService).joinGameByCode(2L, "ABC123");
    }

    @Test
    void leaveGame_validGameId_leavesGame() throws Exception {
        mockMvc.perform(post("/api/games/1/leave")
                .header("X-User-Id", "1"))
                .andExpect(status().isNoContent());

        verify(gameService).leaveGame(1L, 1L);
    }

    @Test
    void getGame_validGameId_returnsGameResponse() throws Exception {
        GameResponse gameResponse = new GameResponse();
        setField(gameResponse, "gameId", 1L);
        gameResponse.setGameCode("ABC123");
        gameResponse.setStatus("WAITING");
        gameResponse.setMaxPlayers(4);

        when(gameService.getGame(1L)).thenReturn(gameResponse);

        mockMvc.perform(get("/api/games/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(1))
                .andExpect(jsonPath("$.gameCode").value("ABC123"));

        verify(gameService).getGame(1L);
    }

    @Test
    void startRound_validGameId_startsRoundAndReturnsGameState() throws Exception {
        GameStateResponse gameState = new GameStateResponse();
        gameState.gameId = 1L;
        gameState.round = new GameStateResponse.RoundView();
        gameState.round.status = "ACTIVE";

        when(roundService.getGameState(1L, 1L)).thenReturn(gameState);

        mockMvc.perform(post("/api/games/1/start")
                .header("X-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(1))
                .andExpect(jsonPath("$.round.status").value("ACTIVE"));

        verify(roundService).startRound(1L);
        verify(roundService).getGameState(1L, 1L);
    }

    @Test
    void getGameState_validGameId_returnsCurrentGameState() throws Exception {
        GameStateResponse gameState = new GameStateResponse();
        gameState.gameId = 1L;
        gameState.round = new GameStateResponse.RoundView();
        gameState.round.status = "ACTIVE";

        when(roundService.getGameState(1L, 1L)).thenReturn(gameState);

        mockMvc.perform(get("/api/games/1/state")
                .header("X-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(1))
                .andExpect(jsonPath("$.round.status").value("ACTIVE"));

        verify(roundService).getGameState(1L, 1L);
    }

    @Test
    void flipInitialCard_validRequest_flipsCardAndReturnsGameState() throws Exception {
        FlipInitialRequest request = new FlipInitialRequest();
        setField(request, "position", 0);

        GameStateResponse gameState = new GameStateResponse();
        gameState.gameId = 1L;

        when(roundService.getGameState(1L, 1L)).thenReturn(gameState);

        mockMvc.perform(post("/api/games/1/actions/flip-initial")
                .header("X-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(1));

        verify(gameActionService).flipInitialCard(1L, 1L, 0);
        verify(roundService).getGameState(1L, 1L);
    }

    @Test
    void drawCard_validRequest_drawsCardAndReturnsGameState() throws Exception {
        DrawCardRequest request = new DrawCardRequest();
        setField(request, "source", "DECK");

        GameStateResponse gameState = new GameStateResponse();
        gameState.gameId = 1L;

        when(roundService.getGameState(1L, 1L)).thenReturn(gameState);

        mockMvc.perform(post("/api/games/1/actions/draw")
                .header("X-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(1));

        verify(gameActionService).drawCard(1L, 1L, "DECK");
        verify(roundService).getGameState(1L, 1L);
    }

    @Test
    void swapCard_validRequest_swapsCardAndReturnsGameState() throws Exception {
        SwapCardRequest request = new SwapCardRequest();
        setField(request, "position", 2);

        GameStateResponse gameState = new GameStateResponse();
        gameState.gameId = 1L;

        when(roundService.getGameState(1L, 1L)).thenReturn(gameState);

        mockMvc.perform(post("/api/games/1/actions/swap")
                .header("X-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(1));

        verify(gameActionService).swapCard(1L, 1L, 2);
        verify(roundService).getGameState(1L, 1L);
    }

    @Test
    void discardCard_validRequest_discardsCardAndReturnsGameState() throws Exception {
        DiscardCardRequest request = new DiscardCardRequest();
        setField(request, "flipPosition", 1);

        GameStateResponse gameState = new GameStateResponse();
        gameState.gameId = 1L;

        when(roundService.getGameState(1L, 1L)).thenReturn(gameState);

        mockMvc.perform(post("/api/games/1/actions/discard")
                .header("X-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(1));

        verify(gameActionService).discardCard(1L, 1L, 1);
        verify(roundService).getGameState(1L, 1L);
    }

    // Helper method to set fields reflectively
    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
