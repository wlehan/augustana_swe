package com.augustana.golf.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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

    private Long requireUserId(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            throw new IllegalArgumentException("Missing X-User-Id header");
        }
        return Long.parseLong(headerValue.trim());
    }

    private Long optionalUserId(String headerValue) {
        return (headerValue == null || headerValue.isBlank()) ? null : Long.parseLong(headerValue.trim());
    }


    @PostMapping
    public ResponseEntity<GameResponse> createGame(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestBody(required = false) CreateGameRequest req) {
        Long userId = requireUserId(userIdHeader);
        int maxPlayers = (req == null) ? 4 : req.getMaxPlayers();
        return ResponseEntity.ok(gameService.createGame(userId, maxPlayers));
    }

    @PostMapping("/join")
    public ResponseEntity<GameResponse> joinGame(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestBody JoinGameRequest req) {
        Long userId = requireUserId(userIdHeader);
        return ResponseEntity.ok(gameService.joinGameByCode(userId, req.getGameCode()));
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameResponse> getGame(@PathVariable Long gameId) {
        return ResponseEntity.ok(gameService.getGame(gameId));
    }

    @PostMapping("/{gameId}/start")
    public ResponseEntity<GameStateResponse> startRound(
            @PathVariable Long gameId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        Long userId = optionalUserId(userIdHeader);
        roundService.startRound(gameId);
        return ResponseEntity.ok(roundService.getGameState(gameId, userId));
    }

    @GetMapping("/{gameId}/state")
    public ResponseEntity<GameStateResponse> getGameState(
            @PathVariable Long gameId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        Long userId = optionalUserId(userIdHeader);
        return ResponseEntity.ok(roundService.getGameState(gameId, userId));
    }


    
    @PostMapping("/{gameId}/actions/flip-initial")
    public ResponseEntity<GameStateResponse> flipInitialCard(
            @PathVariable Long gameId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestBody FlipInitialRequest req) {
        Long userId = requireUserId(userIdHeader);
        gameActionService.flipInitialCard(gameId, userId, req.getPosition());
        return ResponseEntity.ok(roundService.getGameState(gameId, userId));
    }

    
    @PostMapping("/{gameId}/actions/draw")
    public ResponseEntity<GameStateResponse> drawCard(
            @PathVariable Long gameId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestBody DrawCardRequest req) {
        Long userId = requireUserId(userIdHeader);
        gameActionService.drawCard(gameId, userId, req.getSource());
        return ResponseEntity.ok(roundService.getGameState(gameId, userId));
    }

    
    @PostMapping("/{gameId}/actions/swap")
    public ResponseEntity<GameStateResponse> swapCard(
            @PathVariable Long gameId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestBody SwapCardRequest req) {
        Long userId = requireUserId(userIdHeader);
        gameActionService.swapCard(gameId, userId, req.getPosition());
        return ResponseEntity.ok(roundService.getGameState(gameId, userId));
    }

    
    @PostMapping("/{gameId}/actions/discard")
    public ResponseEntity<GameStateResponse> discardCard(
            @PathVariable Long gameId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestBody DiscardCardRequest req) {
        Long userId = requireUserId(userIdHeader);
        gameActionService.discardCard(gameId, userId, req.getFlipPosition());
        return ResponseEntity.ok(roundService.getGameState(gameId, userId));
    }
}
