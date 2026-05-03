package com.augustana.golf.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.augustana.golf.domain.model.Round;

/**
 * Persistence access for finding the current and historical rounds of a game.
 */
public interface RoundRepository extends JpaRepository<Round, Long> {

    Optional<Round> findTopByGame_GameIdOrderByRoundNumberDesc(Long gameId);

    Optional<Round> findByGame_GameIdAndStatus(Long gameId, Round.Status status);

    List<Round> findByGame_GameIdAndStatusIn(Long gameId, List<Round.Status> statuses);

    List<Round> findByGame_GameIdOrderByRoundNumberAsc(Long gameId);
}
