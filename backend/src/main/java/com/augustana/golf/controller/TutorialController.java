package com.augustana.golf.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.augustana.golf.security.CustomUserPrincipal;
import com.augustana.golf.domain.dto.TutorialStateResponse;
import com.augustana.golf.service.TutorialService;

/**
 * REST surface for the tutorial feature.
 *
 * <h3>Endpoint overview</h3>
 * <pre>
 * POST /api/tutorial/start              -> create game + bot, start round, return step WELCOME
 * GET  /api/tutorial/{gameId}/state     -> re-derive current step from live game state
 * POST /api/tutorial/{gameId}/bot-flip  -> bot does its 2 initial flips (call after human flips 2)
 * POST /api/tutorial/{gameId}/bot-turn  -> bot executes a full random turn (call when it's bot's turn)
 * </pre>
 */
@RestController
@RequestMapping("/api/tutorial")
public class TutorialController {

    private final TutorialService tutorialService;

    public TutorialController(TutorialService tutorialService) {
        this.tutorialService = tutorialService;
    }

    /**
     * Creates a tutorial game for the given user and starts the first round.
     *
     * <p>Response includes the full {@link TutorialStateResponse} with
     * {@code currentStep = WELCOME}.
     */
    @PostMapping("/start")
    public ResponseEntity<TutorialStateResponse> startTutorial(
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        Long userId = principal.getUserId();
        return ResponseEntity.ok(tutorialService.startTutorial(userId));
    }

    /**
     * Returns the current tutorial state without mutating anything.
     *
     * <p>Call this after every human action to get the next step hint.
     *
     * @param gameId        the tutorial game's ID (returned by /start)
     */
    @GetMapping("/{gameId}/state")
    public ResponseEntity<TutorialStateResponse> getState(
            @PathVariable Long gameId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        Long userId = principal.getUserId();
        return ResponseEntity.ok(tutorialService.getCurrentState(gameId, userId));
    }

    /**
     * Triggers the bot's 2 initial card flips and transitions the round to ACTIVE.
     *
     * @param gameId        the tutorial game's ID
     */
    @PostMapping("/{gameId}/bot-flip")
    public ResponseEntity<TutorialStateResponse> botFlip(
            @PathVariable Long gameId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        Long userId = principal.getUserId();
        return ResponseEntity.ok(tutorialService.botFlipInitial(gameId, userId));
    }

    /**
     * Executes a complete random bot turn: draw -> swap or discard+flip -> advance turn.
     *
     * @param gameId        the tutorial game's ID
     */
    @PostMapping("/{gameId}/bot-turn")
    public ResponseEntity<TutorialStateResponse> botTurn(
            @PathVariable Long gameId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        Long userId = principal.getUserId();
        return ResponseEntity.ok(tutorialService.executeBotTurn(gameId, userId));
    }
}
