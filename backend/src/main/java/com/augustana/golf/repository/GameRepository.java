package com.augustana.golf.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.augustana.golf.domain.model.Game;

/**
 * Persistence access for lobby records and their public join codes.
 */
public interface GameRepository extends JpaRepository<Game, Long> {
    Optional<Game> findByGameCode(String gameCode);
    boolean existsByGameCode(String gameCode);
}
