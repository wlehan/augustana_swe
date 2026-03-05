package com.augustana.golf.domain.dto;

public class CreateGameRequest {
    private int maxPlayers = 4;
    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }
}