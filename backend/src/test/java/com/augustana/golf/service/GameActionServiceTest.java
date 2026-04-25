package com.augustana.golf.service;

import com.augustana.golf.domain.model.*;
import com.augustana.golf.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameActionServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private GamePlayerRepository gamePlayerRepository;

    @Mock
    private RoundRepository roundRepository;

    @Mock
    private GolfCardRepository golfCardRepository;

    @Mock
    private RoundScoreRepository roundScoreRepository;

    private GameActionService gameActionService;

    @BeforeEach
    void setUp() {
        gameActionService = new GameActionService(
                gameRepository,
                gamePlayerRepository,
                roundRepository,
                golfCardRepository,
                roundScoreRepository,
                null
        );
    }

    @Test
    void flipInitialCard_success_setsCardFaceUp() {
        Game game = new Game();
        setField(game, "gameId", 1L);

        User user = new User();
        user.setUserId(10L);

        GamePlayer player = new GamePlayer();
        setField(player, "gamePlayerId", 2L);
        player.setUser(user);
        player.setGame(game);

        Round round = new Round();
        setField(round, "roundId", 3L);
        round.setGame(game);
        round.setStatus(Round.Status.SETUP);
        round.setCurrentTurnGamePlayer(player);

        GolfCard card = new GolfCard();
        card.setRound(round);
        card.setOwnerGamePlayer(player);
        card.setPosition(2);
        card.setPile(GolfCard.Pile.GRID);
        card.setFaceUp(false);

        when(gamePlayerRepository.findByGame_GameIdAndUser_UserId(1L, 10L))
                .thenReturn(Optional.of(player));
        when(roundRepository.findTopByGame_GameIdOrderByRoundNumberDesc(1L))
                .thenReturn(Optional.of(round));
        when(golfCardRepository.countByRound_RoundIdAndOwnerGamePlayer_GamePlayerIdAndPileAndFaceUp(
                3L, 2L, GolfCard.Pile.GRID, true)).thenReturn(0L, 2L);
        when(golfCardRepository.findByRound_RoundIdAndOwnerGamePlayer_GamePlayerIdAndPositionAndPile(
                3L, 2L, 2, GolfCard.Pile.GRID)).thenReturn(Optional.of(card));

        gameActionService.flipInitialCard(1L, 10L, 2);

        assertTrue(card.isFaceUp());
        verify(golfCardRepository).save(card);
    }

    @Test
    void drawCard_stock_success_movesCardToHand() {
        Game game = new Game();
        setField(game, "gameId", 1L);

        User user = new User();
        user.setUserId(10L);

        GamePlayer player = new GamePlayer();
        setField(player, "gamePlayerId", 2L);
        player.setUser(user);

        Round round = new Round();
        setField(round, "roundId", 3L);
        round.setGame(game);
        round.setStatus(Round.Status.ACTIVE);
        round.setCurrentTurnGamePlayer(player);

        GolfCard drawnCard = new GolfCard();
        drawnCard.setPile(GolfCard.Pile.DRAW);
        drawnCard.setRound(round);

        when(gamePlayerRepository.findByGame_GameIdAndUser_UserId(1L, 10L)).thenReturn(Optional.of(player));
        when(roundRepository.findTopByGame_GameIdOrderByRoundNumberDesc(1L)).thenReturn(Optional.of(round));
        when(golfCardRepository.countByRound_RoundIdAndOwnerGamePlayer_GamePlayerIdAndPile(
                3L, 2L, GolfCard.Pile.HAND)).thenReturn(0L);
        when(golfCardRepository.findTopByRound_RoundIdAndPileOrderByDrawOrderAsc(3L, GolfCard.Pile.DRAW))
                .thenReturn(Optional.of(drawnCard));

        gameActionService.drawCard(1L, 10L, "STOCK");

        assertEquals(GolfCard.Pile.HAND, drawnCard.getPile());
        assertEquals(player, drawnCard.getOwnerGamePlayer());
        assertEquals("STOCK", round.getCurrentDrawSource());
        verify(golfCardRepository).save(drawnCard);
        verify(roundRepository).save(round);
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void calculateScore_matchesPairsDiscountsBothCards() {
        GolfCard a1 = new GolfCard();
        a1.setPosition(1);
        a1.setRank(GolfCard.Rank.FIVE);

        GolfCard a4 = new GolfCard();
        a4.setPosition(4);
        a4.setRank(GolfCard.Rank.FIVE);

        GolfCard b2 = new GolfCard();
        b2.setPosition(2);
        b2.setRank(GolfCard.Rank.NINE);

        GolfCard b5 = new GolfCard();
        b5.setPosition(5);
        b5.setRank(GolfCard.Rank.NINE);

        GolfCard c3 = new GolfCard();
        c3.setPosition(3);
        c3.setRank(GolfCard.Rank.ACE);

        GolfCard c6 = new GolfCard();
        c6.setPosition(6);
        c6.setRank(GolfCard.Rank.KING);

        int score = GameActionService.calculateScore(List.of(a1, a4, b2, b5, c3, c6));

        assertEquals(1, score);
    }
}
