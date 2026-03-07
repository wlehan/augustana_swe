package com.augustana.golf.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.augustana.golf.domain.model.GolfCard;

public interface GolfCardRepository extends JpaRepository<GolfCard, Long> {

    List<GolfCard> findByRound_RoundId(Long roundId);

    List<GolfCard> findByRound_RoundIdAndOwnerGamePlayer_GamePlayerIdOrderByPositionAsc(
        Long roundId,
        Long gamePlayerId
    );

    List<GolfCard> findByRound_RoundIdAndPileOrderByDrawOrderAsc(
        Long roundId,
        GolfCard.Pile pile
    );

    Optional<GolfCard> findTopByRound_RoundIdAndPileOrderByDrawOrderAsc(
        Long roundId,
        GolfCard.Pile pile
    );

    Optional<GolfCard> findTopByRound_RoundIdAndPileOrderByDrawOrderDesc(
        Long roundId,
        GolfCard.Pile pile
    );
}