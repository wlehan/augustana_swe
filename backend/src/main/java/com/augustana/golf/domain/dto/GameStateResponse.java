package com.augustana.golf.domain.dto;

import java.util.List;

/**
 * Full table state returned while a round is in progress.
 *
 * <p>The fields are public DTO properties to keep Jackson serialization simple
 * and to mirror the shape consumed by the React game board.</p>
 */
public class GameStateResponse {
    public Long gameId;
    public String gameCode;
    public String gameStatus;
    public Integer currentRound;

    public RoundView round;
    public List<PlayerBoardView> players;

    public List<RoundScoreSummary> allRoundScores;

    /**
     * Round-level state shared by every player viewing the table.
     */
    public static class RoundView {
        public Long roundId;
        public String status;
        public Long currentTurnUserId;
        public Long currentTurnGamePlayerId;
        public Long finalTurnTriggeredByGamePlayerId;

        public String currentDrawSource;
        public Integer drawPileCount;
        public CardView discardTop;
    }

    /**
     * One player's board as visible to the current requester.
     */
    public static class PlayerBoardView {
        public Long userId;
        public String username;
        public Long gamePlayerId;
        public Integer seatNumber;
        public Integer totalScore;
        public Integer roundScore;

        public Integer initialFlipsCount;

        public CardView heldCard;
        public List<CardView> cards;
    }

    /**
     * Card data that may be partially hidden when the viewer is not allowed to
     * know its rank and suit.
     */
    public static class CardView {
        public Integer position;
        public Boolean faceUp;
        public Boolean revealedToViewer;
        public String suit;
        public String rank;
    }

    /**
     * Completed score rows grouped by round for the ledger.
     */
    public static class RoundScoreSummary {
        public int roundNumber;
        public List<PerPlayerRoundScore> perPlayerScores;
    }

    /**
     * One player's score within a completed round.
     */
    public static class PerPlayerRoundScore {
        public Long gamePlayerId;
        public int score;
    }
}
