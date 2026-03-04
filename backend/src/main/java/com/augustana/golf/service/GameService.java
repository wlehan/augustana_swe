package com.augustana.golf.service;

import java.security.SecureRandom;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.augustana.golf.domain.dto.GameResponse;
import com.augustana.golf.domain.model.Game;
import com.augustana.golf.domain.model.GamePlayer;
import com.augustana.golf.domain.model.User;
import com.augustana.golf.exception.ApiException;
import com.augustana.golf.repository.GamePlayerRepository;
import com.augustana.golf.repository.GameRepository;
import com.augustana.golf.repository.UserRepository;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final GamePlayerRepository gamePlayerRepository;
    private final UserRepository userRepository;
    private final SecureRandom rng = new SecureRandom();

    public GameService(GameRepository gameRepository,
                       GamePlayerRepository gamePlayerRepository,
                       UserRepository userRepository) {
        this.gameRepository = gameRepository;
        this.gamePlayerRepository = gamePlayerRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public GameResponse createGame(Long userId, int maxPlayers) {
        if (maxPlayers < 2 || maxPlayers > 8) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "maxPlayers must be between 2 and 8");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        Game game = new Game();
        game.setMaxPlayers(maxPlayers);
        game.setStatus(Game.Status.WAITING);
        game.setGameCode(generateUniqueCode());

        Game saved = gameRepository.save(game);

        GamePlayer host = new GamePlayer();
        host.setGame(saved);
        host.setUser(user);
        host.setSeatNumber(1);
        host.setTotalScore(0);
        gamePlayerRepository.save(host);

        return toResponse(saved.getGameId());
    }

    @Transactional
    public GameResponse joinGameByCode(Long userId, String gameCode) {
        if (gameCode == null || gameCode.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "gameCode is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        Game game = gameRepository.findByGameCode(gameCode.trim())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Game not found"));

        if (game.getStatus() != Game.Status.WAITING) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Game is not joinable");
        }

        // already joined?
        if (gamePlayerRepository.findByGame_GameIdAndUser_UserId(game.getGameId(), userId).isPresent()) {
            return toResponse(game.getGameId());
        }

        long count = gamePlayerRepository.countByGame_GameId(game.getGameId());
        if (count >= game.getMaxPlayers()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Game is full");
        }

        int nextSeat = gamePlayerRepository.maxSeatNumber(game.getGameId()) + 1;

        GamePlayer gp = new GamePlayer();
        gp.setGame(game);
        gp.setUser(user);
        gp.setSeatNumber(nextSeat);
        gp.setTotalScore(0);
        gamePlayerRepository.save(gp);

        return toResponse(game.getGameId());
    }

    @Transactional(readOnly = true)
    public GameResponse getGame(Long gameId) {
        return toResponse(gameId);
    }

    private GameResponse toResponse(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Game not found"));

        List<GamePlayer> players = gamePlayerRepository.findByGame_GameIdOrderBySeatNumberAsc(gameId);

        GameResponse resp = new GameResponse();
        resp.setGameId(game.getGameId());
        resp.setGameCode(game.getGameCode());
        resp.setStatus(game.getStatus().name());
        resp.setMaxPlayers(game.getMaxPlayers());
        resp.setCurrentRound(game.getCurrentRound() == null ? 0 : game.getCurrentRound());

        resp.setPlayers(players.stream()
                .map(p -> new GameResponse.PlayerInGame(
                        p.getUser().getUserId(),
                        p.getUser().getUsername(),
                        p.getSeatNumber(),
                        p.getTotalScore() == null ? 0 : p.getTotalScore()
                ))
                .toList());

        return resp;
    }

    private String generateUniqueCode() {
        // 6-char uppercase code
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        for (int attempt = 0; attempt < 20; attempt++) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) sb.append(chars.charAt(rng.nextInt(chars.length())));
            String code = sb.toString();
            if (!gameRepository.existsByGameCode(code)) return code;
        }
        throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not generate unique game code");
    }
}