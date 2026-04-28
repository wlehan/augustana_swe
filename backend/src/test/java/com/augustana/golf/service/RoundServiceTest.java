package com.augustana.golf.service;

import com.augustana.golf.domain.dto.GameStateResponse;
import com.augustana.golf.domain.model.*;
import com.augustana.golf.exception.ApiException;
import com.augustana.golf.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoundServiceTest {

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

    @InjectMocks
    private RoundService roundService;

    @Test
    void startRound_success_createsRoundAndDealsCards() {
        Game game = new Game();
        setField(game, "gameId", 100L);
        game.setMaxPlayers(4);
        game.setStatus(Game.Status.WAITING);

        GamePlayer player1 = createPlayer(1L, 10L, 1);
        GamePlayer player2 = createPlayer(2L, 20L, 2);

        when(gameRepository.findById(100L)).thenReturn(Optional.of(game));
        when(gamePlayerRepository.findByGame_GameIdOrderBySeatNumberAsc(100L)).thenReturn(List.of(player1, player2));
        when(roundRepository.findByGame_GameIdAndStatusIn(eq(100L), any())).thenReturn(List.of());
        when(roundRepository.findTopByGame_GameIdOrderByRoundNumberDesc(100L)).thenReturn(Optional.empty());
        when(roundRepository.save(any(Round.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(golfCardRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Round round = roundService.startRound(100L);

        assertEquals(1, round.getRoundNumber());
        assertEquals(Round.Status.SETUP, round.getStatus());
        assertNotNull(round.getCurrentTurnGamePlayer());
        assertEquals(player1, round.getCurrentTurnGamePlayer());
        verify(golfCardRepository).saveAll(any());
        verify(gameRepository).save(game);
    }

    @Test
    void startRound_alreadyActiveRound_throwsApiException() {
        Game game = new Game();
        setField(game, "gameId", 100L);

        when(gameRepository.findById(100L)).thenReturn(Optional.of(game));
        when(gamePlayerRepository.findByGame_GameIdOrderBySeatNumberAsc(100L))
                .thenReturn(List.of(createPlayer(1L, 10L, 1), createPlayer(2L, 20L, 2)));
        when(roundRepository.findByGame_GameIdAndStatusIn(eq(100L), any())).thenReturn(List.of(new Round()));

        ApiException exception = assertThrows(ApiException.class, () -> roundService.startRound(100L));
        assertTrue(exception.getMessage().contains("A round is already in progress"));
    }

    @Test
    void getGameState_returnsVisibleStateForRequestingUser() {
        Game game = new Game();
        setField(game, "gameId", 100L);
        game.setGameCode("CODE1");
        game.setStatus(Game.Status.IN_PROGRESS);
        game.setCurrentRound(1);

        User user = new User();
        user.setUserId(10L);
        user.setUsername("player1");

        GamePlayer player = new GamePlayer();
        setField(player, "gamePlayerId", 1L);
        player.setUser(user);
        player.setSeatNumber(1);
        player.setTotalScore(0);

        Round round = new Round();
        setField(round, "roundId", 500L);
        round.setGame(game);
        round.setRoundNumber(1);
        round.setStatus(Round.Status.ACTIVE);
        round.setCurrentTurnGamePlayer(player);
        round.setCurrentDrawSource("STOCK");

        GolfCard discardCard = new GolfCard();
        discardCard.setPile(GolfCard.Pile.DISCARD);
        discardCard.setDrawOrder(1);
        discardCard.setFaceUp(true);
        discardCard.setSuit(GolfCard.Suit.HEARTS);
        discardCard.setRank(GolfCard.Rank.ACE);

        GolfCard handCard = new GolfCard();
        handCard.setPile(GolfCard.Pile.HAND);
        handCard.setOwnerGamePlayer(player);
        handCard.setFaceUp(false);
        handCard.setSuit(GolfCard.Suit.SPADES);
        handCard.setRank(GolfCard.Rank.KING);

        GolfCard gridCard = new GolfCard();
        gridCard.setPile(GolfCard.Pile.GRID);
        gridCard.setOwnerGamePlayer(player);
        gridCard.setPosition(1);
        gridCard.setFaceUp(false);
        gridCard.setSuit(GolfCard.Suit.CLUBS);
        gridCard.setRank(GolfCard.Rank.TWO);

        when(gameRepository.findById(100L)).thenReturn(Optional.of(game));
        when(roundRepository.findTopByGame_GameIdOrderByRoundNumberDesc(100L)).thenReturn(Optional.of(round));
        when(gamePlayerRepository.findByGame_GameIdOrderBySeatNumberAsc(100L)).thenReturn(List.of(player));
        when(golfCardRepository.findByRound_RoundId(500L)).thenReturn(List.of(discardCard, handCard, gridCard));
        when(roundScoreRepository.findByRound_Game_GameIdOrderByRound_RoundNumberAsc(100L)).thenReturn(List.of());
        when(roundRepository.findByGame_GameIdOrderByRoundNumberAsc(100L)).thenReturn(List.of(round));
        when(roundScoreRepository.findByRound_RoundId(500L)).thenReturn(List.of());

        GameStateResponse response = roundService.getGameState(100L, 10L);

        assertEquals("IN_PROGRESS", response.gameStatus);
        assertNotNull(response.round);
        assertNotNull(response.round.discardTop);
        assertEquals("HEARTS", response.round.discardTop.suit);
        assertEquals("ACE", response.round.discardTop.rank);
        assertEquals(1, response.players.size());
        assertNotNull(response.players.get(0).heldCard);
        assertEquals("SPADES", response.players.get(0).heldCard.suit);
    }

    @Test
    void startRound_gameNotFound_throwsApiException() {
        when(gameRepository.findById(100L)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> roundService.startRound(100L));
        assertTrue(exception.getMessage().contains("Game not found"));
    }

    @Test
    void startRound_noPlayers_throwsApiException() {
        Game game = new Game();
        setField(game, "gameId", 100L);

        when(gameRepository.findById(100L)).thenReturn(Optional.of(game));
        when(gamePlayerRepository.findByGame_GameIdOrderBySeatNumberAsc(100L)).thenReturn(List.of());

        ApiException exception = assertThrows(ApiException.class, () -> roundService.startRound(100L));
        assertTrue(exception.getMessage().contains("At least 2 players are required"));
    }

    @Test
    void startRound_onePlayer_throwsApiException() {
        Game game = new Game();
        setField(game, "gameId", 100L);

        when(gameRepository.findById(100L)).thenReturn(Optional.of(game));
        when(gamePlayerRepository.findByGame_GameIdOrderBySeatNumberAsc(100L))
                .thenReturn(List.of(createPlayer(1L, 10L, 1)));

        ApiException exception = assertThrows(ApiException.class, () -> roundService.startRound(100L));
        assertTrue(exception.getMessage().contains("At least 2 players are required"));
    }

    @Test
    void startRound_nextRoundNumberIncrements() {
        Game game = new Game();
        setField(game, "gameId", 100L);
        game.setMaxPlayers(4);
        game.setStatus(Game.Status.WAITING);

        GamePlayer player1 = createPlayer(1L, 10L, 1);
        GamePlayer player2 = createPlayer(2L, 20L, 2);

        Round previousRound = new Round();
        previousRound.setRoundNumber(1);

        when(gameRepository.findById(100L)).thenReturn(Optional.of(game));
        when(gamePlayerRepository.findByGame_GameIdOrderBySeatNumberAsc(100L)).thenReturn(List.of(player1, player2));
        when(roundRepository.findByGame_GameIdAndStatusIn(eq(100L), any())).thenReturn(List.of());
        when(roundRepository.findTopByGame_GameIdOrderByRoundNumberDesc(100L)).thenReturn(Optional.of(previousRound));
        when(roundRepository.save(any(Round.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(golfCardRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Round round = roundService.startRound(100L);

        assertEquals(2, round.getRoundNumber());
    }

    @Test
    void getGameState_gameNotFound_throwsApiException() {
        when(gameRepository.findById(100L)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> roundService.getGameState(100L, 10L));
        assertTrue(exception.getMessage().contains("Game not found"));
    }

    @Test
    void getGameState_noRound_throwsApiException() {
        Game game = new Game();
        setField(game, "gameId", 100L);

        when(gameRepository.findById(100L)).thenReturn(Optional.of(game));
        when(roundRepository.findTopByGame_GameIdOrderByRoundNumberDesc(100L)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> roundService.getGameState(100L, 10L));
        assertTrue(exception.getMessage().contains("No round found for game"));
    }

    @Test
    void startRound_withMultiplePlayers_dealsCorrectly() {
        Game game = new Game();
        setField(game, "gameId", 100L);
        game.setMaxPlayers(4);
        game.setStatus(Game.Status.WAITING);

        GamePlayer player1 = createPlayer(1L, 10L, 1);
        GamePlayer player2 = createPlayer(2L, 20L, 2);
        List<GolfCard> savedCards = new ArrayList<>();

        when(gameRepository.findById(100L)).thenReturn(Optional.of(game));
        when(gamePlayerRepository.findByGame_GameIdOrderBySeatNumberAsc(100L)).thenReturn(List.of(player1, player2));
        when(roundRepository.findByGame_GameIdAndStatusIn(eq(100L), any())).thenReturn(List.of());
        when(roundRepository.findTopByGame_GameIdOrderByRoundNumberDesc(100L)).thenReturn(Optional.empty());
        when(roundRepository.save(any(Round.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(golfCardRepository.saveAll(any())).thenAnswer(invocation -> captureSavedCards(invocation.getArgument(0), savedCards));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Round round = roundService.startRound(100L);

        assertEquals(1, round.getRoundNumber());
        assertEquals(Round.Status.SETUP, round.getStatus());

        assertEquals(52, savedCards.size());
        assertEquals(12, savedCards.stream().filter(c -> c.getPile() == GolfCard.Pile.GRID).count());
        assertEquals(39, savedCards.stream().filter(c -> c.getPile() == GolfCard.Pile.DRAW).count());
        assertEquals(1, savedCards.stream().filter(c -> c.getPile() == GolfCard.Pile.DISCARD).count());
        assertEquals(6, savedCards.stream().filter(c -> isGridCardFor(c, player1)).count());
        assertEquals(6, savedCards.stream().filter(c -> isGridCardFor(c, player2)).count());
    }

    @Test
    void startRound_withFourPlayers_dealsSixGridCardsEachAndOneDiscard() {
        Game game = new Game();
        setField(game, "gameId", 100L);
        game.setMaxPlayers(4);
        game.setStatus(Game.Status.WAITING);

        GamePlayer player1 = createPlayer(1L, 10L, 1);
        GamePlayer player2 = createPlayer(2L, 20L, 2);
        GamePlayer player3 = createPlayer(3L, 30L, 3);
        GamePlayer player4 = createPlayer(4L, 40L, 4);
        List<GolfCard> savedCards = new ArrayList<>();

        when(gameRepository.findById(100L)).thenReturn(Optional.of(game));
        when(gamePlayerRepository.findByGame_GameIdOrderBySeatNumberAsc(100L))
                .thenReturn(List.of(player1, player2, player3, player4));
        when(roundRepository.findByGame_GameIdAndStatusIn(eq(100L), any())).thenReturn(List.of());
        when(roundRepository.findTopByGame_GameIdOrderByRoundNumberDesc(100L)).thenReturn(Optional.empty());
        when(roundRepository.save(any(Round.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(golfCardRepository.saveAll(any())).thenAnswer(invocation -> captureSavedCards(invocation.getArgument(0), savedCards));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Round round = roundService.startRound(100L);

        assertEquals(player1, round.getCurrentTurnGamePlayer());

        assertEquals(52, savedCards.size());
        assertEquals(24, savedCards.stream().filter(c -> c.getPile() == GolfCard.Pile.GRID).count());
        assertEquals(27, savedCards.stream().filter(c -> c.getPile() == GolfCard.Pile.DRAW).count());
        assertEquals(1, savedCards.stream().filter(c -> c.getPile() == GolfCard.Pile.DISCARD).count());
        assertEquals(6, savedCards.stream().filter(c -> isGridCardFor(c, player1)).count());
        assertEquals(6, savedCards.stream().filter(c -> isGridCardFor(c, player2)).count());
        assertEquals(6, savedCards.stream().filter(c -> isGridCardFor(c, player3)).count());
        assertEquals(6, savedCards.stream().filter(c -> isGridCardFor(c, player4)).count());
    }

    @Test
    void getGameState_withRoundScores_includesScores() {
        Game game = new Game();
        setField(game, "gameId", 100L);
        game.setGameCode("CODE1");
        game.setStatus(Game.Status.IN_PROGRESS);
        game.setCurrentRound(1);

        User user = new User();
        user.setUserId(10L);
        user.setUsername("player1");

        GamePlayer player = new GamePlayer();
        setField(player, "gamePlayerId", 1L);
        player.setUser(user);
        player.setSeatNumber(1);
        player.setTotalScore(0);

        Round round = new Round();
        setField(round, "roundId", 500L);
        round.setGame(game);
        round.setRoundNumber(1);
        round.setStatus(Round.Status.ACTIVE);
        round.setCurrentTurnGamePlayer(player);

        RoundScore score = new RoundScore();
        score.setRound(round);
        score.setGamePlayer(player);
        score.setScore(10);

        when(gameRepository.findById(100L)).thenReturn(Optional.of(game));
        when(roundRepository.findTopByGame_GameIdOrderByRoundNumberDesc(100L)).thenReturn(Optional.of(round));
        when(gamePlayerRepository.findByGame_GameIdOrderBySeatNumberAsc(100L)).thenReturn(List.of(player));
        when(golfCardRepository.findByRound_RoundId(500L)).thenReturn(List.of());
        when(roundScoreRepository.findByRound_Game_GameIdOrderByRound_RoundNumberAsc(100L)).thenReturn(List.of(score));
        when(roundRepository.findByGame_GameIdOrderByRoundNumberAsc(100L)).thenReturn(List.of(round));
        when(roundScoreRepository.findByRound_RoundId(500L)).thenReturn(List.of(score));

        GameStateResponse response = roundService.getGameState(100L, 10L);

        assertNotNull(response.allRoundScores);
        assertEquals(1, response.allRoundScores.size());
        assertEquals(1, response.allRoundScores.get(0).roundNumber);
        assertEquals(1, response.allRoundScores.get(0).perPlayerScores.size());
        assertEquals(10, response.allRoundScores.get(0).perPlayerScores.get(0).score);
        assertEquals(10, response.players.get(0).roundScore);
    }

    @Test
    void getGameState_otherUser_noHeldCard() {
        Game game = new Game();
        setField(game, "gameId", 100L);
        game.setGameCode("CODE1");
        game.setStatus(Game.Status.IN_PROGRESS);
        game.setCurrentRound(1);

        User user = new User();
        user.setUserId(10L);
        user.setUsername("player1");

        GamePlayer player = new GamePlayer();
        setField(player, "gamePlayerId", 1L);
        player.setUser(user);
        player.setSeatNumber(1);
        player.setTotalScore(0);

        Round round = new Round();
        setField(round, "roundId", 500L);
        round.setGame(game);
        round.setRoundNumber(1);
        round.setStatus(Round.Status.ACTIVE);
        round.setCurrentTurnGamePlayer(player);

        GolfCard handCard = new GolfCard();
        handCard.setPile(GolfCard.Pile.HAND);
        handCard.setOwnerGamePlayer(player);
        handCard.setFaceUp(false);
        handCard.setSuit(GolfCard.Suit.SPADES);
        handCard.setRank(GolfCard.Rank.KING);

        when(gameRepository.findById(100L)).thenReturn(Optional.of(game));
        when(roundRepository.findTopByGame_GameIdOrderByRoundNumberDesc(100L)).thenReturn(Optional.of(round));
        when(gamePlayerRepository.findByGame_GameIdOrderBySeatNumberAsc(100L)).thenReturn(List.of(player));
        when(golfCardRepository.findByRound_RoundId(500L)).thenReturn(List.of(handCard));
        when(roundScoreRepository.findByRound_Game_GameIdOrderByRound_RoundNumberAsc(100L)).thenReturn(List.of());
        when(roundRepository.findByGame_GameIdOrderByRoundNumberAsc(100L)).thenReturn(List.of(round));
        when(roundScoreRepository.findByRound_RoundId(500L)).thenReturn(List.of());

        GameStateResponse response = roundService.getGameState(100L, 20L); // Different user

        assertEquals(1, response.players.size());
        assertNull(response.players.get(0).heldCard); // Not revealed to other user
    }

    @Test
    void getGameState_withFaceUpCards_countsFlips() {
        Game game = new Game();
        setField(game, "gameId", 100L);
        game.setGameCode("CODE1");
        game.setStatus(Game.Status.IN_PROGRESS);
        game.setCurrentRound(1);

        User user = new User();
        user.setUserId(10L);
        user.setUsername("player1");

        GamePlayer player = new GamePlayer();
        setField(player, "gamePlayerId", 1L);
        player.setUser(user);
        player.setSeatNumber(1);
        player.setTotalScore(0);

        Round round = new Round();
        setField(round, "roundId", 500L);
        round.setGame(game);
        round.setRoundNumber(1);
        round.setStatus(Round.Status.ACTIVE);
        round.setCurrentTurnGamePlayer(player);

        GolfCard gridCard1 = new GolfCard();
        gridCard1.setPile(GolfCard.Pile.GRID);
        gridCard1.setOwnerGamePlayer(player);
        gridCard1.setPosition(1);
        gridCard1.setFaceUp(true);

        GolfCard gridCard2 = new GolfCard();
        gridCard2.setPile(GolfCard.Pile.GRID);
        gridCard2.setOwnerGamePlayer(player);
        gridCard2.setPosition(2);
        gridCard2.setFaceUp(false);

        when(gameRepository.findById(100L)).thenReturn(Optional.of(game));
        when(roundRepository.findTopByGame_GameIdOrderByRoundNumberDesc(100L)).thenReturn(Optional.of(round));
        when(gamePlayerRepository.findByGame_GameIdOrderBySeatNumberAsc(100L)).thenReturn(List.of(player));
        when(golfCardRepository.findByRound_RoundId(500L)).thenReturn(List.of(gridCard1, gridCard2));
        when(roundScoreRepository.findByRound_Game_GameIdOrderByRound_RoundNumberAsc(100L)).thenReturn(List.of());
        when(roundRepository.findByGame_GameIdOrderByRoundNumberAsc(100L)).thenReturn(List.of(round));
        when(roundScoreRepository.findByRound_RoundId(500L)).thenReturn(List.of());

        GameStateResponse response = roundService.getGameState(100L, 10L);

        assertEquals(1, response.players.size());
        assertEquals(1, response.players.get(0).initialFlipsCount);
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

    private static GamePlayer createPlayer(Long gamePlayerId, Long userId, int seatNumber) {
        User user = new User();
        user.setUserId(userId);

        GamePlayer player = new GamePlayer();
        setField(player, "gamePlayerId", gamePlayerId);
        player.setSeatNumber(seatNumber);
        player.setUser(user);
        player.setTotalScore(0);
        return player;
    }

    private static Iterable<GolfCard> captureSavedCards(Iterable<GolfCard> cards, List<GolfCard> savedCards) {
        for (GolfCard card : cards) {
            savedCards.add(card);
        }
        return cards;
    }

    private static boolean isGridCardFor(GolfCard card, GamePlayer player) {
        return card.getPile() == GolfCard.Pile.GRID
                && card.getOwnerGamePlayer() != null
                && card.getOwnerGamePlayer().getGamePlayerId().equals(player.getGamePlayerId());
    }
}
