package com.augustana.golf.domain.model;

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
 * One physical card within a round. A card moves between piles by changing its
 * pile, owner, grid position, and draw order fields.
 */
@Entity
@Table(name = "cards")
public class GolfCard {

    public enum Suit {
        CLUBS,
        DIAMONDS,
        HEARTS,
        SPADES
    }

    public enum Rank {
        ACE,
        TWO,
        THREE,
        FOUR,
        FIVE,
        SIX,
        SEVEN,
        EIGHT,
        NINE,
        TEN,
        JACK,
        QUEEN,
        KING
    }

    /**
     * GRID cards belong to a player board, DRAW cards are ordered stock, DISCARD
     * cards are ordered by stack position, and HAND is the temporary held card.
     */
    public enum Pile {
        GRID,
        DRAW,
        DISCARD,
        HAND
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_id")
    private Long cardId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id", nullable = false, foreignKey = @ForeignKey(name = "fk_card_round"))
    private Round round;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_game_player_id", foreignKey = @ForeignKey(name = "fk_card_owner_gp"))
    private GamePlayer ownerGamePlayer;

    @Enumerated(EnumType.STRING)
    @Column(name = "suit", nullable = false, length = 10)
    private Suit suit;

    @Enumerated(EnumType.STRING)
    @Column(name = "rank", nullable = false, length = 10)
    private Rank rank;

    @Column(name = "position")
    private Integer position;

    @Column(name = "is_face_up", nullable = false)
    private boolean faceUp = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "pile", nullable = false, length = 20)
    private Pile pile = Pile.GRID;

    @Column(name = "draw_order")
    private Integer drawOrder;

    public Long getCardId() {
        return cardId;
    }

    public Round getRound() {
        return round;
    }

    public void setRound(Round round) {
        this.round = round;
    }

    public GamePlayer getOwnerGamePlayer() {
        return ownerGamePlayer;
    }

    public void setOwnerGamePlayer(GamePlayer ownerGamePlayer) {
        this.ownerGamePlayer = ownerGamePlayer;
    }

    public Suit getSuit() {
        return suit;
    }

    public void setSuit(Suit suit) {
        this.suit = suit;
    }

    public Rank getRank() {
        return rank;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public boolean isFaceUp() {
        return faceUp;
    }

    public void setFaceUp(boolean faceUp) {
        this.faceUp = faceUp;
    }

    public Pile getPile() {
        return pile;
    }

    public void setPile(Pile pile) {
        this.pile = pile;
    }

    public Integer getDrawOrder() {
        return drawOrder;
    }

    public void setDrawOrder(Integer drawOrder) {
        this.drawOrder = drawOrder;
    }
}
