package com.augustana.golf.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.augustana.golf.domain.model.Game;

public interface GameRepository extends JpaRepository<Game, Long> {
    Optional<Game> findByGameCode(String gameCode);
    boolean existsByGameCode(String gameCode);
}