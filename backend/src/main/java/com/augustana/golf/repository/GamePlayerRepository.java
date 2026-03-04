package com.augustana.golf.repository;

import com.augustana.golf.domain.model.GamePlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GamePlayerRepository extends JpaRepository<GamePlayer, Long> {

    List<GamePlayer> findByGame_GameIdOrderBySeatNumberAsc(Long gameId);

    Optional<GamePlayer> findByGame_GameIdAndUser_UserId(Long gameId, Long userId);

    @Query("select coalesce(max(gp.seatNumber), 0) from GamePlayer gp where gp.game.gameId = :gameId")
    int maxSeatNumber(Long gameId);

    long countByGame_GameId(Long gameId);
}