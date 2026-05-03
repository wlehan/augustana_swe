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
 * REST endpoints for the guided tutorial flow.
 *
 * <p>The tutorial reuses the normal game endpoints for human actions, while
 * these endpoints start the tutorial and advance the bot when needed.</p>
 */
@RestController
@RequestMapping("/api/tutorial")
public class TutorialController {

    private final TutorialService tutorialService;

    public TutorialController(TutorialService tutorialService) {
        this.tutorialService = tutorialService;
    }

    /**
     * Creates a tutorial game for the current user and starts the first round.
     */
    @PostMapping("/start")
    public ResponseEntity<TutorialStateResponse> startTutorial(
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        Long userId = principal.getUserId();
        return ResponseEntity.ok(tutorialService.startTutorial(userId));
    }

    /**
     * Returns the current tutorial state without mutating anything.
     */
    @GetMapping("/{gameId}/state")
    public ResponseEntity<TutorialStateResponse> getState(
            @PathVariable Long gameId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        Long userId = principal.getUserId();
        return ResponseEntity.ok(tutorialService.getCurrentState(gameId, userId));
    }

    /**
     * Triggers the bot's two setup flips once the human has flipped theirs.
     */
    @PostMapping("/{gameId}/bot-flip")
    public ResponseEntity<TutorialStateResponse> botFlip(
            @PathVariable Long gameId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        Long userId = principal.getUserId();
        return ResponseEntity.ok(tutorialService.botFlipInitial(gameId, userId));
    }

    /**
     * Executes one complete random bot turn.
     */
    @PostMapping("/{gameId}/bot-turn")
    public ResponseEntity<TutorialStateResponse> botTurn(
            @PathVariable Long gameId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        Long userId = principal.getUserId();
        return ResponseEntity.ok(tutorialService.executeBotTurn(gameId, userId));
    }
}
