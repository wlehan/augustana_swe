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
        game.setMaxPlayers(1);
        game.setStatus(Game.Status.WAITING);

        User user = new User();
        user.setUserId(10L);

        GamePlayer player = new GamePlayer();
        setField(player, "gamePlayerId", 1L);
        player.setSeatNumber(1);
        player.setUser(user);

        when(gameRepository.findById(100L)).thenReturn(Optional.of(game));
        when(gamePlayerRepository.findByGame_GameIdOrderBySeatNumberAsc(100L)).thenReturn(List.of(player));
        when(roundRepository.findByGame_GameIdAndStatusIn(eq(100L), any())).thenReturn(List.of());
        when(roundRepository.findTopByGame_GameIdOrderByRoundNumberDesc(100L)).thenReturn(Optional.empty());
        when(roundRepository.save(any(Round.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(golfCardRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Round round = roundService.startRound(100L);

        assertEquals(1, round.getRoundNumber());
        assertEquals(Round.Status.SETUP, round.getStatus());
        assertNotNull(round.getCurrentTurnGamePlayer());
        verify(golfCardRepository).saveAll(any());
        verify(gameRepository).save(game);
    }

    @Test
    void startRound_alreadyActiveRound_throwsApiException() {
        Game game = new Game();
        setField(game, "gameId", 100L);

        when(gameRepository.findById(100L)).thenReturn(Optional.of(game));
        when(gamePlayerRepository.findByGame_GameIdOrderBySeatNumberAsc(100L)).thenReturn(List.of(new GamePlayer()));
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
        assertTrue(exception.getMessage().contains("At least 1 player is required"));
    }

    @Test
    void startRound_nextRoundNumberIncrements() {
        Game game = new Game();
        setField(game, "gameId", 100L);
        game.setMaxPlayers(1);
        game.setStatus(Game.Status.WAITING);

        User user = new User();
        user.setUserId(10L);

        GamePlayer player = new GamePlayer();
        setField(player, "gamePlayerId", 1L);
        player.setSeatNumber(1);
        player.setUser(user);

        Round previousRound = new Round();
        previousRound.setRoundNumber(1);

        when(gameRepository.findById(100L)).thenReturn(Optional.of(game));
        when(gamePlayerRepository.findByGame_GameIdOrderBySeatNumberAsc(100L)).thenReturn(List.of(player));
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
        game.setMaxPlayers(2);
        game.setStatus(Game.Status.WAITING);

        User user1 = new User();
        user1.setUserId(10L);

        GamePlayer player1 = new GamePlayer();
        setField(player1, "gamePlayerId", 1L);
        player1.setSeatNumber(1);
        player1.setUser(user1);

        User user2 = new User();
        user2.setUserId(20L);

        GamePlayer player2 = new GamePlayer();
        setField(player2, "gamePlayerId", 2L);
        player2.setSeatNumber(2);
        player2.setUser(user2);

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
        verify(golfCardRepository).saveAll(any()); // 12 grid + draw pile
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
}
