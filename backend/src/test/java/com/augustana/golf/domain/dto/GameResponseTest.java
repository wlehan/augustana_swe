package com.augustana.golf.domain.dto;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameResponseTest {

    @Test
    void gameResponse_gettersAndSetters_work() {
        GameResponse response = new GameResponse();
        response.setGameId(100L);
        response.setGameCode("ABC123");
        response.setStatus("WAITING");
        response.setMaxPlayers(4);
        response.setCurrentRound(1);

        assertEquals(100L, response.getGameId());
        assertEquals("ABC123", response.getGameCode());
        assertEquals("WAITING", response.getStatus());
        assertEquals(4, response.getMaxPlayers());
        assertEquals(1, response.getCurrentRound());
    }

    @Test
    void gameResponse_setGameId_works() {
        GameResponse response = new GameResponse();
        response.setGameId(55L);
        assertEquals(55L, response.getGameId());
    }

    @Test
    void gameResponse_setGameCode_works() {
        GameResponse response = new GameResponse();
        response.setGameCode("XYZ789");
        assertEquals("XYZ789", response.getGameCode());
    }

    @Test
    void gameResponse_setStatus_works() {
        GameResponse response = new GameResponse();
        response.setStatus("IN_PROGRESS");
        assertEquals("IN_PROGRESS", response.getStatus());
    }

    @Test
    void gameResponse_setMaxPlayers_works() {
        GameResponse response = new GameResponse();
        response.setMaxPlayers(2);
        assertEquals(2, response.getMaxPlayers());
    }

    @Test
    void gameResponse_setCurrentRound_works() {
        GameResponse response = new GameResponse();
        response.setCurrentRound(3);
        assertEquals(3, response.getCurrentRound());
    }

    @Test
    void gameResponse_setPlayers_works() {
        GameResponse response = new GameResponse();
        List<GameResponse.PlayerInGame> players = new ArrayList<>();
        players.add(new GameResponse.PlayerInGame(101L, 1L, "alice", 1, 0));
        players.add(new GameResponse.PlayerInGame(102L, 2L, "bob", 2, 5));
        
        response.setPlayers(players);
        
        assertEquals(2, response.getPlayers().size());
        assertEquals("alice", response.getPlayers().get(0).username());
        assertEquals(5, response.getPlayers().get(1).totalScore());
    }

    @Test
    void gameResponse_playerInGame_record_hasAllFields() {
        GameResponse.PlayerInGame player = new GameResponse.PlayerInGame(100L, 10L, "player1", 1, 50);
        
        assertEquals(100L, player.gamePlayerId());
        assertEquals(10L, player.userId());
        assertEquals("player1", player.username());
        assertEquals(1, player.seatNumber());
        assertEquals(50, player.totalScore());
    }

    @Test
    void gameResponse_playerInGame_multiplePlayers() {
        GameResponse response = new GameResponse();
        List<GameResponse.PlayerInGame> players = List.of(
            new GameResponse.PlayerInGame(101L, 1L, "alice", 1, 10),
            new GameResponse.PlayerInGame(102L, 2L, "bob", 2, 20),
            new GameResponse.PlayerInGame(103L, 3L, "charlie", 3, 30)
        );
        response.setPlayers(players);
        
        assertEquals(3, response.getPlayers().size());
        assertEquals("bob", response.getPlayers().get(1).username());
        assertEquals(30, response.getPlayers().get(2).totalScore());
    }

    @Test
    void gameResponse_setPlayers_toNull() {
        GameResponse response = new GameResponse();
        response.setPlayers(null);
        assertNull(response.getPlayers());
    }

    @Test
    void gameResponse_setPlayers_toEmptyList() {
        GameResponse response = new GameResponse();
        response.setPlayers(new ArrayList<>());
        assertNotNull(response.getPlayers());
        assertEquals(0, response.getPlayers().size());
    }
}
