package com.augustana.golf.controller;

import com.augustana.golf.domain.dto.TutorialStateResponse;
import com.augustana.golf.domain.dto.GameStateResponse;
import com.augustana.golf.domain.model.TutorialStep;
import com.augustana.golf.security.CustomUserPrincipal;
import com.augustana.golf.security.JwtAuthenticationFilter;
import com.augustana.golf.service.TutorialService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(TutorialController.class)
@AutoConfigureMockMvc(addFilters = false)
class TutorialControllerTest {

    private RequestPostProcessor authenticatedUser(Long userId) {
    return request -> {
        CustomUserPrincipal principal = new CustomUserPrincipal(
                userId,
                "test@example.com",
                "password"
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        request.setUserPrincipal(authentication);

        return request;
    };
}

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TutorialService tutorialService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void startTutorial_validUser_startsTutorialAndReturnsWelcomeState() throws Exception {
        GameStateResponse gameState = new GameStateResponse();
        gameState.gameId = 1L;
        gameState.round = new GameStateResponse.RoundView();
        gameState.round.status = "SETUP";

        TutorialStateResponse response = new TutorialStateResponse();
        response.gameState = gameState;
        response.currentStep = TutorialStep.WELCOME;
        response.humanFlipsCompleted = 0;
        response.allPlayersReady = false;
        response.botJustMoved = false;

        when(tutorialService.startTutorial(1L)).thenReturn(response);

        mockMvc.perform(post("/api/tutorial/start")
                .with(authenticatedUser(1L))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameState.gameId").value(1))
                .andExpect(jsonPath("$.currentStep").value("WELCOME"))
                .andExpect(jsonPath("$.humanFlipsCompleted").value(0))
                .andExpect(jsonPath("$.allPlayersReady").value(false));

        verify(tutorialService).startTutorial(1L);
    }

    @Test
    void getState_validGameIdAndUser_returnsCurrentTutorialState() throws Exception {
        GameStateResponse gameState = new GameStateResponse();
        gameState.gameId = 1L;
        gameState.round = new GameStateResponse.RoundView();
        gameState.round.status = "SETUP";

        TutorialStateResponse response = new TutorialStateResponse();
        response.gameState = gameState;
        response.currentStep = TutorialStep.FLIP_FIRST;
        response.humanFlipsCompleted = 0;
        response.allPlayersReady = false;
        response.botJustMoved = false;

        when(tutorialService.getCurrentState(1L, 1L)).thenReturn(response);

        mockMvc.perform(get("/api/tutorial/1/state")
                .with(authenticatedUser(1L))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameState.gameId").value(1))
                .andExpect(jsonPath("$.currentStep").value("FLIP_FIRST"));

        verify(tutorialService).getCurrentState(1L, 1L);
    }

    @Test
    void botFlip_validGameIdAndUser_executesBotFlipsAndReturnsUpdatedState() throws Exception {
        GameStateResponse gameState = new GameStateResponse();
        gameState.gameId = 1L;
        gameState.round = new GameStateResponse.RoundView();
        gameState.round.status = "ACTIVE";

        TutorialStateResponse response = new TutorialStateResponse();
        response.gameState = gameState;
        response.currentStep = TutorialStep.YOUR_TURN_DRAW;
        response.humanFlipsCompleted = 2;
        response.allPlayersReady = true;
        response.botJustMoved = true;

        when(tutorialService.botFlipInitial(1L, 1L)).thenReturn(response);

        mockMvc.perform(post("/api/tutorial/1/bot-flip")
                .with(authenticatedUser(1L))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStep").value("YOUR_TURN_DRAW"))
                .andExpect(jsonPath("$.humanFlipsCompleted").value(2))
                .andExpect(jsonPath("$.allPlayersReady").value(true))
                .andExpect(jsonPath("$.botJustMoved").value(true));

        verify(tutorialService).botFlipInitial(1L, 1L);
    }

    @Test
    void botTurn_validGameIdAndUser_executesBotTurnAndReturnsUpdatedState() throws Exception {
        GameStateResponse gameState = new GameStateResponse();
        gameState.gameId = 1L;
        gameState.round = new GameStateResponse.RoundView();
        gameState.round.status = "ACTIVE";
        gameState.round.currentTurnUserId = 1L;

        TutorialStateResponse response = new TutorialStateResponse();
        response.gameState = gameState;
        response.currentStep = TutorialStep.YOUR_TURN_DRAW;
        response.humanFlipsCompleted = 2;
        response.allPlayersReady = true;
        response.botJustMoved = true;

        when(tutorialService.executeBotTurn(1L, 1L)).thenReturn(response);

        mockMvc.perform(post("/api/tutorial/1/bot-turn")
                .with(authenticatedUser(1L))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStep").value("YOUR_TURN_DRAW"))
                .andExpect(jsonPath("$.botJustMoved").value(true));

        verify(tutorialService).executeBotTurn(1L, 1L);
    }

    @Test
    void botTurn_transitionsToFinalTurns_returnsUpdatedState() throws Exception {
        GameStateResponse gameState = new GameStateResponse();
        gameState.gameId = 1L;
        gameState.round = new GameStateResponse.RoundView();
        gameState.round.status = "FINAL_TURNS";
        gameState.round.currentTurnUserId = 99L;

        TutorialStateResponse response = new TutorialStateResponse();
        response.gameState = gameState;
        response.currentStep = TutorialStep.FINAL_TURNS;
        response.humanFlipsCompleted = 2;
        response.allPlayersReady = true;
        response.botJustMoved = true;

        when(tutorialService.executeBotTurn(1L, 1L)).thenReturn(response);

        mockMvc.perform(post("/api/tutorial/1/bot-turn")
                .with(authenticatedUser(1L))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStep").value("FINAL_TURNS"));

        verify(tutorialService).executeBotTurn(1L, 1L);
    }

    @Test
    void roundComplete_roundIsScored_returnsTutorialCompleteStep() throws Exception {
        GameStateResponse gameState = new GameStateResponse();
        gameState.gameId = 1L;
        gameState.round = new GameStateResponse.RoundView();
        gameState.round.status = "SCORED";

        TutorialStateResponse response = new TutorialStateResponse();
        response.gameState = gameState;
        response.currentStep = TutorialStep.TUTORIAL_COMPLETE;
        response.humanFlipsCompleted = 2;
        response.allPlayersReady = true;
        response.botJustMoved = false;

        when(tutorialService.getCurrentState(1L, 1L)).thenReturn(response);

        mockMvc.perform(get("/api/tutorial/1/state")
                .with(authenticatedUser(1L))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStep").value("TUTORIAL_COMPLETE"));
    }
}
