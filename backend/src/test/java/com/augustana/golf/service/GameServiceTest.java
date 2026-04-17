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
