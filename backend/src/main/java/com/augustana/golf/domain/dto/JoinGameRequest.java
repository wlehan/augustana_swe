package com.augustana.golf.domain.dto;

/**
 * Request body for joining an existing lobby by its player-facing code.
 */
public class JoinGameRequest {
    private String gameCode;
    public String getGameCode() { return gameCode; }
    public void setGameCode(String gameCode) { this.gameCode = gameCode; }
}
