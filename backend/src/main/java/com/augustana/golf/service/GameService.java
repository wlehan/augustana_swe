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

/**
 * Handles lobby-level game operations before and around a round of play.
 *
 * <p>The game code is the player-facing join key, while seat numbers are the
 * stable order used by the game board and turn logic. Active games keep their
 * player rows even when someone leaves so that a reconnect can restore the same
 * seat and cards.</p>
 */
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

    /**
     * Creates a waiting lobby and assigns the creator to host seat 1.
     */
    @Transactional
    public GameResponse createGame(Long userId, int maxPlayers) {
        if (maxPlayers < 2 || maxPlayers > 4) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "maxPlayers must be between 2 and 4");
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

    /**
     * Adds a user to a waiting lobby, or returns the existing lobby if the user
     * is rejoining with the same account.
     */
    @Transactional
    public GameResponse joinGameByCode(Long userId, String gameCode) {
        if (gameCode == null || gameCode.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "gameCode is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        Game game = gameRepository.findByGameCode(gameCode.trim())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Game not found"));

        if (gamePlayerRepository.findByGame_GameIdAndUser_UserId(game.getGameId(), userId).isPresent()) {
            return toResponse(game.getGameId());
        }

        if (game.getStatus() != Game.Status.WAITING) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Game is not joinable");
        }

        List<GamePlayer> existingPlayers = gamePlayerRepository.findByGame_GameIdOrderBySeatNumberAsc(game.getGameId());

        if (existingPlayers.size() >= game.getMaxPlayers()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Game is full");
        }

        int nextSeat = existingPlayers.stream()
                .mapToInt(GamePlayer::getSeatNumber)
                .max()
                .orElse(0) + 1;

        GamePlayer gp = new GamePlayer();
        gp.setGame(game);
        gp.setUser(user);
        gp.setSeatNumber(nextSeat);
        gp.setTotalScore(0);
        gamePlayerRepository.save(gp);

        return toResponse(game.getGameId());
    }

    /**
     * Removes a player from a waiting lobby. Once play has started, the player
     * row is kept so the user can rejoin with the game code and recover their
     * original seat.
     */
    @Transactional
    public void leaveGame(Long gameId, Long userId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Game not found"));

        GamePlayer player = gamePlayerRepository.findByGame_GameIdAndUser_UserId(gameId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Player is not in this game"));

        if (game.getStatus() != Game.Status.WAITING) {
            return;
        }

        List<GamePlayer> players = gamePlayerRepository.findByGame_GameIdOrderBySeatNumberAsc(gameId);
        List<GamePlayer> remainingPlayers = players.stream()
                .filter(p -> !p.getGamePlayerId().equals(player.getGamePlayerId()))
                .toList();

        gamePlayerRepository.delete(player);
        gamePlayerRepository.flush();

        if (remainingPlayers.isEmpty()) {
            gameRepository.delete(game);
            return;
        }

        if (player.getSeatNumber() == 1) {
            GamePlayer nextHost = remainingPlayers.get(0);
            nextHost.setSeatNumber(1);
            gamePlayerRepository.save(nextHost);
        }
    }

    /**
     * Returns the lobby summary used by waiting-room screens.
     */
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
                        p.getGamePlayerId(),
                        p.getUser().getUserId(),
                        p.getUser().getUsername(),
                        p.getSeatNumber(),
                        p.getTotalScore() == null ? 0 : p.getTotalScore()
                ))
                .toList());

        return resp;
    }

    private String generateUniqueCode() {
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
