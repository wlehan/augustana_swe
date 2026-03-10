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
import com.augustana.golf.domain.dto.GameResponse;
import com.augustana.golf.domain.dto.GameStateResponse;
import com.augustana.golf.domain.dto.JoinGameRequest;
import com.augustana.golf.service.GameService;
import com.augustana.golf.service.RoundService;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;
    private final RoundService roundService;

    public GameController(GameService gameService, RoundService roundService) {
        this.gameService = gameService;
        this.roundService = roundService;
    }

    // MVP until JWT/session auth exists
    private Long requireUserId(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            throw new IllegalArgumentException("Missing X-User-Id header");
        }
        return Long.parseLong(headerValue);
    }

    @PostMapping
    public ResponseEntity<GameResponse> createGame(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestBody(required = false) CreateGameRequest req
    ) {
        Long userId = requireUserId(userIdHeader);
        int maxPlayers = (req == null) ? 4 : req.getMaxPlayers();
        return ResponseEntity.ok(gameService.createGame(userId, maxPlayers));
    }

    @PostMapping("/join")
    public ResponseEntity<GameResponse> joinGame(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestBody JoinGameRequest req
    ) {
        Long userId = requireUserId(userIdHeader);
        return ResponseEntity.ok(gameService.joinGameByCode(userId, req.getGameCode()));
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameResponse> getGame(@PathVariable Long gameId) {
        return ResponseEntity.ok(gameService.getGame(gameId));
    }

   @PostMapping("/{gameId}/start")
    public ResponseEntity<GameStateResponse> startRound(@PathVariable Long gameId) {
        roundService.startRound(gameId);
        return ResponseEntity.ok(roundService.getGameState(gameId));
    }

    @GetMapping("/{gameId}/state")
    public ResponseEntity<GameStateResponse> getGameState(@PathVariable Long gameId) {
        return ResponseEntity.ok(roundService.getGameState(gameId));
    }
}