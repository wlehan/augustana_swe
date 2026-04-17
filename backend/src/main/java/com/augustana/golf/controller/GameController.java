package com.augustana.golf.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.augustana.golf.domain.dto.CreateGameRequest;
import com.augustana.golf.domain.dto.DiscardCardRequest;
import com.augustana.golf.domain.dto.DrawCardRequest;
import com.augustana.golf.domain.dto.FlipInitialRequest;
import com.augustana.golf.domain.dto.GameResponse;
import com.augustana.golf.domain.dto.GameStateResponse;
import com.augustana.golf.domain.dto.JoinGameRequest;
import com.augustana.golf.domain.dto.SwapCardRequest;
import com.augustana.golf.security.CustomUserPrincipal;
import com.augustana.golf.service.GameActionService;
import com.augustana.golf.service.GameService;
import com.augustana.golf.service.RoundService;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;
    private final RoundService roundService;
    private final GameActionService gameActionService;

    public GameController(GameService gameService, RoundService roundService, GameActionService gameActionService) {
        this.gameService = gameService;
        this.roundService = roundService;
        this.gameActionService = gameActionService;
    }


    @PostMapping
    public ResponseEntity<GameResponse> createGame(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody(required = false) CreateGameRequest req) {
        Long userId = principal.getUserId();
        int maxPlayers = (req == null) ? 4 : req.getMaxPlayers();
        return ResponseEntity.ok(gameService.createGame(userId, maxPlayers));
    }

    @PostMapping("/join")
    public ResponseEntity<GameResponse> joinGame(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody JoinGameRequest req) {
        Long userId = principal.getUserId();
        return ResponseEntity.ok(gameService.joinGameByCode(userId, req.getGameCode()));
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameResponse> getGame(@PathVariable Long gameId) {
        return ResponseEntity.ok(gameService.getGame(gameId));
    }

    @PostMapping("/{gameId}/start")
    public ResponseEntity<GameStateResponse> startRound(
            @PathVariable Long gameId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        Long userId = principal.getUserId();
        roundService.startRound(gameId);
        return ResponseEntity.ok(roundService.getGameState(gameId, userId));
    }

    @GetMapping("/{gameId}/state")
    public ResponseEntity<GameStateResponse> getGameState(
            @PathVariable Long gameId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        Long userId = principal.getUserId();
        return ResponseEntity.ok(roundService.getGameState(gameId, userId));
    }


    
    @PostMapping("/{gameId}/actions/flip-initial")
    public ResponseEntity<GameStateResponse> flipInitialCard(
            @PathVariable Long gameId,
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody FlipInitialRequest req) {
        Long userId = principal.getUserId();
        gameActionService.flipInitialCard(gameId, userId, req.getPosition());
        return ResponseEntity.ok(roundService.getGameState(gameId, userId));
    }

    
    @PostMapping("/{gameId}/actions/draw")
    public ResponseEntity<GameStateResponse> drawCard(
            @PathVariable Long gameId,
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody DrawCardRequest req) {
        Long userId = principal.getUserId();
        gameActionService.drawCard(gameId, userId, req.getSource());
        return ResponseEntity.ok(roundService.getGameState(gameId, userId));
    }

    
    @PostMapping("/{gameId}/actions/swap")
    public ResponseEntity<GameStateResponse> swapCard(
            @PathVariable Long gameId,
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody SwapCardRequest req) {
        Long userId = principal.getUserId();
        gameActionService.swapCard(gameId, userId, req.getPosition());
        return ResponseEntity.ok(roundService.getGameState(gameId, userId));
    }

    
    @PostMapping("/{gameId}/actions/discard")
    public ResponseEntity<GameStateResponse> discardCard(
            @PathVariable Long gameId,
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody DiscardCardRequest req) {
        Long userId = principal.getUserId();
        gameActionService.discardCard(gameId, userId, req.getFlipPosition());
        return ResponseEntity.ok(roundService.getGameState(gameId, userId));
    }
}
