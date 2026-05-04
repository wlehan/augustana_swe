package com.augustana.golf.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.augustana.golf.domain.model.GolfCard;

/**
 * Persistence access for cards by round, pile, owner, and grid position.
 */
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

    List<GolfCard> findByRound_RoundIdAndPile(
        Long roundId,
        GolfCard.Pile pile
    );

    List<GolfCard> findByRound_RoundIdAndOwnerGamePlayer_GamePlayerIdAndPile(
        Long roundId,
        Long gamePlayerId,
        GolfCard.Pile pile
    );

    Optional<GolfCard> findFirstByRound_RoundIdAndOwnerGamePlayer_GamePlayerIdAndPile(
        Long roundId,
        Long gamePlayerId,
        GolfCard.Pile pile
    );

    Optional<GolfCard> findByRound_RoundIdAndOwnerGamePlayer_GamePlayerIdAndPositionAndPile(
        Long roundId,
        Long gamePlayerId,
        Integer position,
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

    long countByRound_RoundIdAndOwnerGamePlayer_GamePlayerIdAndPile(
        Long roundId,
        Long gamePlayerId,
        GolfCard.Pile pile
    );

    long countByRound_RoundIdAndOwnerGamePlayer_GamePlayerIdAndPileAndFaceUp(
        Long roundId,
        Long gamePlayerId,
        GolfCard.Pile pile,
        boolean faceUp
    );
}
