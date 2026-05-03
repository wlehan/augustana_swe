package com.augustana.golf.domain.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * A single dealt round inside a game. It owns turn status, the player whose turn
 * it is, and the player who triggered final turns.
 */
@Entity
@Table(name = "rounds")
public class Round {

    /**
     * SETUP is the initial two-card reveal, ACTIVE is normal turns,
     * FINAL_TURNS gives everyone else one last turn, and SCORED is terminal.
     */
    public enum Status {
        SETUP,
        ACTIVE,
        FINAL_TURNS,
        SCORED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "round_id")
    private Long roundId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false, foreignKey = @ForeignKey(name = "fk_round_game"))
    private Game game;

    @Column(name = "round_number", nullable = false)
    private int roundNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.SETUP;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "current_turn_game_player_id",
        foreignKey = @ForeignKey(name = "FK_rounds_current_turn_game_player")
    )
    private GamePlayer currentTurnGamePlayer;

    @Column(name = "dealer_seat")
    private Integer dealerSeat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "final_turn_triggered_by_game_player_id",
        foreignKey = @ForeignKey(name = "FK_rounds_final_turn_triggered_by_game_player")
    )
    private GamePlayer finalTurnTriggeredByGamePlayer;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    
    @Column(name = "current_draw_source", length = 10)
    private String currentDrawSource;

    public Long getRoundId() {
        return roundId;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public GamePlayer getCurrentTurnGamePlayer() {
        return currentTurnGamePlayer;
    }

    public void setCurrentTurnGamePlayer(GamePlayer currentTurnGamePlayer) {
        this.currentTurnGamePlayer = currentTurnGamePlayer;
    }

    public Integer getDealerSeat() {
        return dealerSeat;
    }

    public void setDealerSeat(Integer dealerSeat) {
        this.dealerSeat = dealerSeat;
    }

    public GamePlayer getFinalTurnTriggeredByGamePlayer() {
        return finalTurnTriggeredByGamePlayer;
    }

    public void setFinalTurnTriggeredByGamePlayer(GamePlayer finalTurnTriggeredByGamePlayer) {
        this.finalTurnTriggeredByGamePlayer = finalTurnTriggeredByGamePlayer;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public String getCurrentDrawSource() {
        return currentDrawSource;
    }

    public void setCurrentDrawSource(String currentDrawSource) {
        this.currentDrawSource = currentDrawSource;
    }
}
