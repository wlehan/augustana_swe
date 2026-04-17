package com.augustana.golf.service;

import com.augustana.golf.domain.dto.GameResponse;
import com.augustana.golf.domain.model.Game;
import com.augustana.golf.domain.model.GamePlayer;
import com.augustana.golf.domain.model.User;
import com.augustana.golf.exception.ApiException;
import com.augustana.golf.repository.GamePlayerRepository;
import com.augustana.golf.repository.GameRepository;
import com.augustana.golf.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private GamePlayerRepository gamePlayerRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GameService gameService;

    @Test
    void createGame_success_returnsGameResponse() {
        User user = new User();
        user.setUserId(10L);
        user.setUsername("alice");

        Game[] savedGame = new Game[1];
        GamePlayer[] savedHost = new GamePlayer[1];

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(gameRepository.existsByGameCode(any())).thenReturn(false);
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> {
            Game game = invocation.getArgument(0);
            setField(game, "gameId", 55L);
            savedGame[0] = game;
            return game;
        });
        when(gameRepository.findById(55L)).thenAnswer(invocation -> Optional.ofNullable(savedGame[0]));
        when(gamePlayerRepository.save(any(GamePlayer.class))).thenAnswer(invocation -> {
            GamePlayer player = invocation.getArgument(0);
            setField(player, "gamePlayerId", 101L);
            savedHost[0] = player;
            return player;
        });
        when(gamePlayerRepository.findByGame_GameIdOrderBySeatNumberAsc(55L))
                .thenAnswer(invocation -> savedHost[0] == null ? List.of() : List.of(savedHost[0]));

        GameResponse response = gameService.createGame(10L, 4);

        assertEquals(55L, response.getGameId());
        assertEquals(4, response.getMaxPlayers());
        assertEquals("WAITING", response.getStatus());
        assertNotNull(response.getGameCode());
        assertEquals(1, response.getPlayers().size());

        ArgumentCaptor<GamePlayer> playerCaptor = ArgumentCaptor.forClass(GamePlayer.class);
        verify(gamePlayerRepository).save(playerCaptor.capture());
        assertEquals(1, playerCaptor.getValue().getSeatNumber());
    }

    @Test
    void createGame_invalidMaxPlayers_throwsApiException() {
        ApiException exception = assertThrows(ApiException.class, () -> gameService.createGame(10L, 5));

        assertTrue(exception.getMessage().contains("maxPlayers must be between 1 and 4"));
        verify(gameRepository, never()).save(any());
    }

    @Test
    void joinGameByCode_gameIsFull_throwsApiException() {
        User user = new User();
        user.setUserId(10L);

        Game game = new Game();
        setField(game, "gameId", 55L);
        game.setMaxPlayers(1);
        game.setStatus(Game.Status.WAITING);

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(gameRepository.findByGameCode("ABC123")).thenReturn(Optional.of(game));
        when(gamePlayerRepository.findByGame_GameIdOrderBySeatNumberAsc(55L))
                .thenReturn(List.of(new GamePlayer()));

        ApiException exception = assertThrows(ApiException.class,
                () -> gameService.joinGameByCode(10L, "ABC123"));

        assertTrue(exception.getMessage().contains("Game is full"));
    }


    @Test
    void joinGameByCode_alreadyJoined_returnsExistingGame() {
        User user = new User();
        user.setUserId(10L);

        Game game = new Game();
        setField(game, "gameId", 55L);
        game.setMaxPlayers(4);
        game.setStatus(Game.Status.WAITING);
        game.setGameCode("ABC123");

        GamePlayer existingPlayer = new GamePlayer();
        setField(existingPlayer, "gamePlayerId", 101L);
        existingPlayer.setGame(game);
        existingPlayer.setUser(user);
        existingPlayer.setSeatNumber(1);

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(gameRepository.findByGameCode("ABC123")).thenReturn(Optional.of(game));
        when(gamePlayerRepository.findByGame_GameIdAndUser_UserId(55L, 10L))
                .thenReturn(Optional.of(existingPlayer));
        when(gameRepository.findById(55L)).thenReturn(Optional.of(game));
        when(gamePlayerRepository.findByGame_GameIdOrderBySeatNumberAsc(55L))
                .thenReturn(List.of(existingPlayer));

        GameResponse response = gameService.joinGameByCode(10L, "ABC123");

        assertEquals(55L, response.getGameId());
        assertEquals("ABC123", response.getGameCode());
        assertEquals(1, response.getPlayers().size());
    }

    @Test
    void createGame_maxPlayersZero_throwsApiException() {
        ApiException exception = assertThrows(ApiException.class, () -> gameService.createGame(10L, 0));

        assertTrue(exception.getMessage().contains("maxPlayers must be between 1 and 4"));
    }

    @Test
    void createGame_userNotFound_throwsApiException() {
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> gameService.createGame(10L, 2));

        assertTrue(exception.getMessage().contains("User not found"));
        verify(gameRepository, never()).save(any());
    }

    @Test
    void joinGameByCode_userNotFound_throwsApiException() {
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class,
                () -> gameService.joinGameByCode(10L, "ABC123"));

        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    void joinGameByCode_gameNotFound_throwsApiException() {
        User user = new User();
        user.setUserId(10L);

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(gameRepository.findByGameCode("ABC123")).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class,
                () -> gameService.joinGameByCode(10L, "ABC123"));

        assertTrue(exception.getMessage().contains("Game not found"));
    }

    @Test
    void joinGameByCode_gameNotWaiting_throwsApiException() {
        User user = new User();
        user.setUserId(10L);

        Game game = new Game();
        setField(game, "gameId", 55L);
        game.setStatus(Game.Status.IN_PROGRESS);

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(gameRepository.findByGameCode("ABC123")).thenReturn(Optional.of(game));

        ApiException exception = assertThrows(ApiException.class,
                () -> gameService.joinGameByCode(10L, "ABC123"));

        assertTrue(exception.getMessage().contains("Game is not joinable"));
    }

    @Test
    void joinGameByCode_success_addsPlayerWithNextSeat() {
        User hostUser = new User();
        hostUser.setUserId(10L);
        hostUser.setUsername("host");

        User joiningUser = new User();
        joiningUser.setUserId(20L);
        joiningUser.setUsername("player2");

        Game game = new Game();
        setField(game, "gameId", 55L);
        game.setMaxPlayers(4);
        game.setStatus(Game.Status.WAITING);
        game.setGameCode("ABC123");

        GamePlayer hostPlayer = new GamePlayer();
        setField(hostPlayer, "gamePlayerId", 101L);
        hostPlayer.setUser(hostUser);
        hostPlayer.setSeatNumber(1);
        hostPlayer.setTotalScore(0);

        GamePlayer newPlayer = new GamePlayer();
        setField(newPlayer, "gamePlayerId", 102L);
        newPlayer.setUser(joiningUser);
        newPlayer.setSeatNumber(2);
        newPlayer.setTotalScore(0);

        when(userRepository.findById(20L)).thenReturn(Optional.of(joiningUser));
        when(gameRepository.findByGameCode("ABC123")).thenReturn(Optional.of(game));
        when(gamePlayerRepository.findByGame_GameIdAndUser_UserId(55L, 20L))
                .thenReturn(Optional.empty());
        when(gamePlayerRepository.findByGame_GameIdOrderBySeatNumberAsc(55L))
                .thenReturn(List.of(hostPlayer));
        
        when(gamePlayerRepository.save(any(GamePlayer.class)))
                .thenReturn(newPlayer);

        when(gameRepository.findById(55L))
                .thenReturn(Optional.of(game));
        when(gamePlayerRepository.findByGame_GameIdOrderBySeatNumberAsc(55L))
                .thenReturn(List.of(hostPlayer, newPlayer));

        GameResponse response = gameService.joinGameByCode(20L, "ABC123");

        assertEquals(55L, response.getGameId());
        assertEquals(2, response.getPlayers().size());
        assertEquals("host", response.getPlayers().get(0).username());
        assertEquals("player2", response.getPlayers().get(1).username());
    }

    @Test
    void getGame_success_returnsGameWithPlayers() {
        User user = new User();
        user.setUserId(10L);
        user.setUsername("alice");

        Game game = new Game();
        setField(game, "gameId", 55L);
        game.setGameCode("ABC123");
        game.setStatus(Game.Status.IN_PROGRESS);
        game.setMaxPlayers(4);
        game.setCurrentRound(1);

        GamePlayer player = new GamePlayer();
        setField(player, "gamePlayerId", 101L);
        player.setUser(user);
        player.setSeatNumber(1);
        player.setTotalScore(50);

        when(gameRepository.findById(55L)).thenReturn(Optional.of(game));
        when(gamePlayerRepository.findByGame_GameIdOrderBySeatNumberAsc(55L))
                .thenReturn(List.of(player));

        GameResponse response = gameService.getGame(55L);

        assertEquals(55L, response.getGameId());
        assertEquals("ABC123", response.getGameCode());
        assertEquals("IN_PROGRESS", response.getStatus());
        assertEquals(4, response.getMaxPlayers());
        assertEquals(1, response.getCurrentRound());
        assertEquals(1, response.getPlayers().size());
        assertEquals("alice", response.getPlayers().get(0).username());
        assertEquals(50, response.getPlayers().get(0).totalScore());
    }

    @Test
    void getGame_notFound_throwsApiException() {
        when(gameRepository.findById(55L)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> gameService.getGame(55L));

        assertTrue(exception.getMessage().contains("Game not found"));
    }

    @Test
    void joinGameByCode_multiplePlayersMultipleSeats() {
        User user1 = new User();
        user1.setUserId(10L);
        user1.setUsername("player1");

        User user2 = new User();
        user2.setUserId(20L);
        user2.setUsername("player2");

        User user3 = new User();
        user3.setUserId(30L);
        user3.setUsername("player3");

        Game game = new Game();
        setField(game, "gameId", 55L);
        game.setMaxPlayers(4);
        game.setStatus(Game.Status.WAITING);
        game.setGameCode("ABC123");

        GamePlayer player1 = new GamePlayer();
        setField(player1, "gamePlayerId", 101L);
        player1.setUser(user1);
        player1.setSeatNumber(1);

        GamePlayer player2 = new GamePlayer();
        setField(player2, "gamePlayerId", 102L);
        player2.setUser(user2);
        player2.setSeatNumber(2);

        when(userRepository.findById(30L)).thenReturn(Optional.of(user3));
        when(gameRepository.findByGameCode("ABC123")).thenReturn(Optional.of(game));
        when(gamePlayerRepository.findByGame_GameIdAndUser_UserId(55L, 30L))
                .thenReturn(Optional.empty());
        when(gamePlayerRepository.findByGame_GameIdOrderBySeatNumberAsc(55L))
                .thenReturn(List.of(player1, player2));

        GamePlayer player3 = new GamePlayer();
        setField(player3, "gamePlayerId", 103L);
        player3.setUser(user3);
        player3.setSeatNumber(3);
        
        when(gamePlayerRepository.save(any(GamePlayer.class)))
                .thenReturn(player3);
        when(gameRepository.findById(55L))
                .thenReturn(Optional.of(game));
        when(gamePlayerRepository.findByGame_GameIdOrderBySeatNumberAsc(55L))
                .thenReturn(List.of(player1, player2, player3));

        GameResponse response = gameService.joinGameByCode(30L, "ABC123");

        assertEquals(3, response.getPlayers().size());
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
