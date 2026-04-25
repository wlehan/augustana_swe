package com.augustana.golf.domain.dto;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameStateResponseTest {

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
