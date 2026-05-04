package com.augustana.golf.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.augustana.golf.domain.model.RoundScore;

/**
 * Persistence access for completed per-player round scores.
 */
public interface RoundScoreRepository extends JpaRepository<RoundScore, Long> {

    List<RoundScore> findByRound_RoundId(Long roundId);

    Optional<RoundScore> findByRound_RoundIdAndGamePlayer_GamePlayerId(Long roundId, Long gamePlayerId);

    List<RoundScore> findByRound_Game_GameIdOrderByRound_RoundNumberAsc(Long gameId);
}
