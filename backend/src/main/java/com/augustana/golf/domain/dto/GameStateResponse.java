package com.augustana.golf.domain.dto;

import java.util.List;

public class GameStateResponse {
    public Long gameId;
    public String gameCode;
    public String gameStatus;
    public Integer currentRound;

    public RoundView round;
    public List<PlayerBoardView> players;

    public static class RoundView {
        public Long roundId;
        public String status;
        public Long currentTurnUserId;
        public Long currentTurnGamePlayerId;
        public Integer drawPileCount;
        public CardView discardTop;
    }

    public static class PlayerBoardView {
        public Long userId;
        public String username;
        public Long gamePlayerId;
        public Integer seatNumber;
        public Integer totalScore;
        public List<CardView> cards;
    }

    public static class CardView {
        public Integer position;
        public Boolean faceUp;
        public String suit;
        public String rank;
    }
}