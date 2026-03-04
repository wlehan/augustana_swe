package com.augustana.golf.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "game_players",
       uniqueConstraints = @UniqueConstraint(name = "uq_game_seat", columnNames = {"game_id", "seat_number"}))
public class GamePlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_player_id")
    private Long gamePlayerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_gp_game"))
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_gp_user"))
    private User user;

    @Column(name = "seat_number", nullable = false)
    private int seatNumber;

    @Column(name = "total_score")
    private Integer totalScore = 0;

    public Long getGamePlayerId() { return gamePlayerId; }
    public Game getGame() { return game; }
    public void setGame(Game game) { this.game = game; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public int getSeatNumber() { return seatNumber; }
    public void setSeatNumber(int seatNumber) { this.seatNumber = seatNumber; }
    public Integer getTotalScore() { return totalScore; }
    public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }
}