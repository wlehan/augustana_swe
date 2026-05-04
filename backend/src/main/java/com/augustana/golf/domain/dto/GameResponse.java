package com.augustana.golf.domain.dto;

import java.util.List;

/**
 * Lightweight lobby response used before a round has started.
 */
public class GameResponse {
    private Long gameId;
    private String gameCode;
    private String status;
    private int maxPlayers;
    private int currentRound;
    private List<PlayerInGame> players;

    /**
     * Player summary shown in lobby lists and host start-game dialogs.
     */
    public record PlayerInGame(Long gamePlayerId, Long userId, String username, int seatNumber, int totalScore) {}

    public Long getGameId() { return gameId; }
    public void setGameId(Long gameId) { this.gameId = gameId; }
    public String getGameCode() { return gameCode; }
    public void setGameCode(String gameCode) { this.gameCode = gameCode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }
    public int getCurrentRound() { return currentRound; }
    public void setCurrentRound(int currentRound) { this.currentRound = currentRound; }
    public List<PlayerInGame> getPlayers() { return players; }
    public void setPlayers(List<PlayerInGame> players) { this.players = players; }
}
