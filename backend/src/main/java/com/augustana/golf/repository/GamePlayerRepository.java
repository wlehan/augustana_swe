package com.augustana.golf.repository;

import com.augustana.golf.domain.model.GamePlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GamePlayerRepository extends JpaRepository<GamePlayer, Long> {

    List<GamePlayer> findByGame_GameIdOrderBySeatNumberAsc(Long gameId);
    List<GamePlayer> findByUser_UserId(Long userId);

    Optional<GamePlayer> findByGame_GameIdAndUser_UserId(Long gameId, Long userId);

    @Query("select coalesce(max(gp.seatNumber), 0) from GamePlayer gp where gp.game.gameId = :gameId")
    int maxSeatNumber(@Param("gameId") Long gameId);

    @Query("select count(gp) from GamePlayer gp where gp.game.gameId = :gameId")
    long countPlayersInGame(@Param("gameId") Long gameId);
}
