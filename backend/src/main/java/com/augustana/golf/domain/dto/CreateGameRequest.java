package com.augustana.golf.domain.dto;

/**
 * Request body for creating a lobby. The backend currently defaults to four
 * seats while allowing the round to start once two players have joined.
 */
public class CreateGameRequest {
    private int maxPlayers = 4;
    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }
}
