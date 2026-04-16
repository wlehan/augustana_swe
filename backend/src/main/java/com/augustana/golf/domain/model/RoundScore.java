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

@Entity
@Table(name = "round_scores")
public class RoundScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id", nullable = false, foreignKey = @ForeignKey(name = "FK_rs_round"))
    private Round round;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_player_id", nullable = false, foreignKey = @ForeignKey(name = "FK_rs_gp"))
    private GamePlayer gamePlayer;

    @Column(name = "score", nullable = false)
    private int score;

    public Long getId() { return id; }

    public Round getRound() { return round; }
    public void setRound(Round round) { this.round = round; }

    public GamePlayer getGamePlayer() { return gamePlayer; }
    public void setGamePlayer(GamePlayer gamePlayer) { this.gamePlayer = gamePlayer; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
}
