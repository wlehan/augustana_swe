package com.augustana.golf.domain.dto;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RequestDtosGameResponseTest {

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

class RequestDtosAuthResponseTest {

    @Test
    void authResponse_record_hasAllFields() {
        AuthResponse response = new AuthResponse(100L, "testuser", "fake-token", "Login successful");
        
        assertEquals(100L, response.userId());
        assertEquals("testuser", response.username());
        assertEquals("Login successful", response.message());
    }

    @Test
    void authResponse_withNullMessage() {
        AuthResponse response = new AuthResponse(100L, "testuser", "fake-token", null);
        
        assertNull(response.message());
    }

    @Test
    void authResponse_multipleInstances_areEqual() {
        AuthResponse response1 = new AuthResponse(100L, "testuser", "fake-token", "Success");
        AuthResponse response2 = new AuthResponse(100L, "testuser", "fake-token", "Success");
        
        assertEquals(response1, response2);
    }

    @Test
    void authResponse_toString_works() {
        AuthResponse response = new AuthResponse(100L, "testuser", "fake-token", "Success");
        String str = response.toString();
        
        assertNotNull(str);
        assertTrue(str.contains("testuser"));
    }
}

class RequestDtosGameStateResponseTest {

    @Test
    void gameStateResponse_cardView_gettersAndSetters() {
        GameStateResponse.CardView card = new GameStateResponse.CardView();
        card.position = 1;
        card.faceUp = true;
        card.revealedToViewer = true;
        card.suit = "HEARTS";
        card.rank = "ACE";
        
        assertEquals(1, card.position);
        assertTrue(card.faceUp);
        assertTrue(card.revealedToViewer);
        assertEquals("HEARTS", card.suit);
        assertEquals("ACE", card.rank);
    }

    @Test
    void gameStateResponse_roundView_gettersAndSetters() {
        GameStateResponse.RoundView round = new GameStateResponse.RoundView();
        round.roundId = 500L;
        round.status = "ACTIVE";
        round.currentTurnUserId = 10L;
        round.currentTurnGamePlayerId = 1L;
        round.currentDrawSource = "STOCK";
        round.drawPileCount = 45;
        
        assertEquals(500L, round.roundId);
        assertEquals("ACTIVE", round.status);
        assertEquals(10L, round.currentTurnUserId);
        assertEquals(1L, round.currentTurnGamePlayerId);
        assertEquals("STOCK", round.currentDrawSource);
        assertEquals(45, round.drawPileCount);
    }

    @Test
    void gameStateResponse_playerBoardView_gettersAndSetters() {
        GameStateResponse.PlayerBoardView player = new GameStateResponse.PlayerBoardView();
        player.userId = 10L;
        player.username = "alice";
        player.gamePlayerId = 1L;
        player.seatNumber = 1;
        player.totalScore = 50;
        player.roundScore = 10;
        player.initialFlipsCount = 2;
        
        assertEquals(10L, player.userId);
        assertEquals("alice", player.username);
        assertEquals(1L, player.gamePlayerId);
        assertEquals(1, player.seatNumber);
        assertEquals(50, player.totalScore);
        assertEquals(10, player.roundScore);
        assertEquals(2, player.initialFlipsCount);
    }

    @Test
    void gameStateResponse_perPlayerRoundScore_gettersAndSetters() {
        GameStateResponse.PerPlayerRoundScore score = new GameStateResponse.PerPlayerRoundScore();
        score.gamePlayerId = 1L;
        score.score = 25;
        
        assertEquals(1L, score.gamePlayerId);
        assertEquals(25, score.score);
    }

    @Test
    void gameStateResponse_roundScoreSummary_gettersAndSetters() {
        GameStateResponse.RoundScoreSummary summary = new GameStateResponse.RoundScoreSummary();
        summary.roundNumber = 1;
        summary.perPlayerScores = new ArrayList<>();
        
        GameStateResponse.PerPlayerRoundScore score1 = new GameStateResponse.PerPlayerRoundScore();
        score1.gamePlayerId = 1L;
        score1.score = 20;
        summary.perPlayerScores.add(score1);
        
        assertEquals(1, summary.roundNumber);
        assertEquals(1, summary.perPlayerScores.size());
        assertEquals(20, summary.perPlayerScores.get(0).score);
    }

    @Test
    void gameStateResponse_main_gettersAndSetters() {
        GameStateResponse response = new GameStateResponse();
        response.gameId = 100L;
        response.gameCode = "ABC123";
        response.gameStatus = "IN_PROGRESS";
        response.currentRound = 1;
        
        assertEquals(100L, response.gameId);
        assertEquals("ABC123", response.gameCode);
        assertEquals("IN_PROGRESS", response.gameStatus);
        assertEquals(1, response.currentRound);
    }

    @Test
    void gameStateResponse_withRoundAndPlayers() {
        GameStateResponse response = new GameStateResponse();
        response.gameId = 100L;
        
        GameStateResponse.RoundView round = new GameStateResponse.RoundView();
        round.roundId = 500L;
        response.round = round;
        
        GameStateResponse.PlayerBoardView player = new GameStateResponse.PlayerBoardView();
        player.userId = 10L;
        response.players = List.of(player);
        
        assertNotNull(response.round);
        assertEquals(500L, response.round.roundId);
        assertEquals(1, response.players.size());
    }

    @Test
    void gameStateResponse_cardView_faceUpFalse() {
        GameStateResponse.CardView card = new GameStateResponse.CardView();
        card.faceUp = false;
        card.revealedToViewer = false;
        
        assertFalse(card.faceUp);
        assertFalse(card.revealedToViewer);
    }

    @Test
    void gameStateResponse_playerBoardView_withMultipleCards() {
        GameStateResponse.PlayerBoardView player = new GameStateResponse.PlayerBoardView();
        
        GameStateResponse.CardView card1 = new GameStateResponse.CardView();
        card1.position = 1;
        card1.faceUp = true;
        
        GameStateResponse.CardView card2 = new GameStateResponse.CardView();
        card2.position = 2;
        card2.faceUp = false;
        
        player.cards = List.of(card1, card2);
        
        assertEquals(2, player.cards.size());
        assertEquals(1, player.cards.get(0).position);
        assertEquals(2, player.cards.get(1).position);
    }
}
