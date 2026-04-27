package com.augustana.golf.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.augustana.golf.domain.model.Game;
import com.augustana.golf.domain.model.GamePlayer;
import com.augustana.golf.domain.model.GolfCard;
import com.augustana.golf.domain.model.Round;
import com.augustana.golf.domain.model.RoundScore;
import com.augustana.golf.exception.ApiException;
import com.augustana.golf.repository.GamePlayerRepository;
import com.augustana.golf.repository.GameRepository;
import com.augustana.golf.repository.GolfCardRepository;
import com.augustana.golf.repository.RoundRepository;
import com.augustana.golf.repository.RoundScoreRepository;

@Service
public class GameActionService {

    private final GameRepository gameRepository;
    private final GamePlayerRepository gamePlayerRepository;
    private final RoundRepository roundRepository;
    private final GolfCardRepository golfCardRepository;
    private final RoundScoreRepository roundScoreRepository;
    private final RoundService roundService;

    public GameActionService(
            GameRepository gameRepository,
            GamePlayerRepository gamePlayerRepository,
            RoundRepository roundRepository,
            GolfCardRepository golfCardRepository,
            RoundScoreRepository roundScoreRepository,
            RoundService roundService) {
        this.gameRepository = gameRepository;
        this.gamePlayerRepository = gamePlayerRepository;
        this.roundRepository = roundRepository;
        this.golfCardRepository = golfCardRepository;
        this.roundScoreRepository = roundScoreRepository;
        this.roundService = roundService;
    }


    
    @Transactional
    public void flipInitialCard(Long gameId, Long userId, int position) {
        if (position < 1 || position > 6) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Position must be 1–6");
        }

        GamePlayer player = getPlayer(gameId, userId);
        Round round = getActiveRound(gameId, Round.Status.SETUP);

        long flippedCount = golfCardRepository
                .countByRound_RoundIdAndOwnerGamePlayer_GamePlayerIdAndPileAndFaceUp(
                        round.getRoundId(), player.getGamePlayerId(), GolfCard.Pile.GRID, true);

        if (flippedCount >= 2) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You have already flipped 2 cards");
        }

        GolfCard card = golfCardRepository
                .findByRound_RoundIdAndOwnerGamePlayer_GamePlayerIdAndPositionAndPile(
                        round.getRoundId(), player.getGamePlayerId(), position, GolfCard.Pile.GRID)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No card at position " + position));

        if (card.isFaceUp()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Card is already face-up");
        }

        card.setFaceUp(true);
        golfCardRepository.save(card);

        checkSetupComplete(gameId, round);
    }

    private void checkSetupComplete(Long gameId, Round round) {
        List<GamePlayer> players = gamePlayerRepository.findByGame_GameIdOrderBySeatNumberAsc(gameId);

        boolean allReady = players.stream().allMatch(p -> {
            long flipped = golfCardRepository
                    .countByRound_RoundIdAndOwnerGamePlayer_GamePlayerIdAndPileAndFaceUp(
                            round.getRoundId(), p.getGamePlayerId(), GolfCard.Pile.GRID, true);
            return flipped >= 2;
        });

        if (allReady) {
            round.setStatus(Round.Status.ACTIVE);
            roundRepository.save(round);
        }
    }


    
    @Transactional
    public void drawCard(Long gameId, Long userId, String source) {
        GamePlayer player = getPlayer(gameId, userId);
        Round round = getActiveRound(gameId, Round.Status.ACTIVE, Round.Status.FINAL_TURNS);

        validateCurrentTurn(round, player);

        long handCards = golfCardRepository
                .countByRound_RoundIdAndOwnerGamePlayer_GamePlayerIdAndPile(
                        round.getRoundId(), player.getGamePlayerId(), GolfCard.Pile.HAND);

        if (handCards > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You already have a card in hand");
        }

        if ("STOCK".equalsIgnoreCase(source)) {
            drawFromStock(player, round);
        } else if ("DISCARD".equalsIgnoreCase(source)) {
            drawFromDiscard(player, round);
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST, "source must be STOCK or DISCARD");
        }
    }

    private void drawFromStock(GamePlayer player, Round round) {
        GolfCard drawn = golfCardRepository
                .findTopByRound_RoundIdAndPileOrderByDrawOrderAsc(round.getRoundId(), GolfCard.Pile.DRAW)
                .orElseGet(() -> {
                    reshuffleDiscard(round);
                    return golfCardRepository
                            .findTopByRound_RoundIdAndPileOrderByDrawOrderAsc(round.getRoundId(), GolfCard.Pile.DRAW)
                            .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "No cards left to draw"));
                });

        drawn.setPile(GolfCard.Pile.HAND);
        drawn.setOwnerGamePlayer(player);
        drawn.setFaceUp(false);
        drawn.setDrawOrder(null);
        golfCardRepository.save(drawn);

        round.setCurrentDrawSource("STOCK");
        roundRepository.save(round);
    }

    private void drawFromDiscard(GamePlayer player, Round round) {
        GolfCard top = golfCardRepository
                .findTopByRound_RoundIdAndPileOrderByDrawOrderDesc(round.getRoundId(), GolfCard.Pile.DISCARD)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Discard pile is empty"));

        top.setPile(GolfCard.Pile.HAND);
        top.setOwnerGamePlayer(player);
        top.setDrawOrder(null);
        golfCardRepository.save(top);

        round.setCurrentDrawSource("DISCARD");
        roundRepository.save(round);
    }

    private void reshuffleDiscard(Round round) {
        GolfCard discardTop = golfCardRepository
                .findTopByRound_RoundIdAndPileOrderByDrawOrderDesc(round.getRoundId(), GolfCard.Pile.DISCARD)
                .orElse(null);

        List<GolfCard> toReshuffle = golfCardRepository
                .findByRound_RoundIdAndPile(round.getRoundId(), GolfCard.Pile.DISCARD)
                .stream()
                .filter(c -> discardTop == null || !c.getCardId().equals(discardTop.getCardId()))
                .collect(Collectors.toList());

        Collections.shuffle(toReshuffle);
        for (int i = 0; i < toReshuffle.size(); i++) {
            GolfCard c = toReshuffle.get(i);
            c.setPile(GolfCard.Pile.DRAW);
            c.setFaceUp(false);
            c.setDrawOrder(i + 1);
            c.setOwnerGamePlayer(null);
            c.setPosition(null);
        }
        golfCardRepository.saveAll(toReshuffle);
    }


    
    @Transactional
    public void swapCard(Long gameId, Long userId, int position) {
        if (position < 1 || position > 6) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Position must be 1–6");
        }

        GamePlayer player = getPlayer(gameId, userId);
        Round round = getActiveRound(gameId, Round.Status.ACTIVE, Round.Status.FINAL_TURNS);

        validateCurrentTurn(round, player);

        GolfCard heldCard = golfCardRepository
                .findFirstByRound_RoundIdAndOwnerGamePlayer_GamePlayerIdAndPile(
                        round.getRoundId(), player.getGamePlayerId(), GolfCard.Pile.HAND)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "No card in hand"));

        GolfCard gridCard = golfCardRepository
                .findByRound_RoundIdAndOwnerGamePlayer_GamePlayerIdAndPositionAndPile(
                        round.getRoundId(), player.getGamePlayerId(), position, GolfCard.Pile.GRID)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No card at position " + position));

        int nextOrder = getNextDiscardOrder(round);
        gridCard.setPile(GolfCard.Pile.DISCARD);
        gridCard.setFaceUp(true);
        gridCard.setPosition(null);
        gridCard.setOwnerGamePlayer(null);
        gridCard.setDrawOrder(nextOrder);
        golfCardRepository.saveAndFlush(gridCard);

        heldCard.setPile(GolfCard.Pile.GRID);
        heldCard.setPosition(position);
        heldCard.setFaceUp(true);
        heldCard.setDrawOrder(null);
        golfCardRepository.save(heldCard);

        round.setCurrentDrawSource(null);
        roundRepository.save(round);

        checkAndAdvanceTurn(gameId, round, player);
    }


    
    @Transactional
    public void discardCard(Long gameId, Long userId, Integer flipPosition) {
        GamePlayer player = getPlayer(gameId, userId);
        Round round = getActiveRound(gameId, Round.Status.ACTIVE, Round.Status.FINAL_TURNS);

        validateCurrentTurn(round, player);

        if (!"STOCK".equals(round.getCurrentDrawSource())) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "You can only discard a card drawn from the stock pile");
        }

        GolfCard heldCard = golfCardRepository
                .findFirstByRound_RoundIdAndOwnerGamePlayer_GamePlayerIdAndPile(
                        round.getRoundId(), player.getGamePlayerId(), GolfCard.Pile.HAND)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "No card in hand"));

        int nextOrder = getNextDiscardOrder(round);
        heldCard.setPile(GolfCard.Pile.DISCARD);
        heldCard.setFaceUp(true);
        heldCard.setPosition(null);
        heldCard.setOwnerGamePlayer(null);
        heldCard.setDrawOrder(nextOrder);
        golfCardRepository.save(heldCard);

        long faceDownCount = golfCardRepository
                .countByRound_RoundIdAndOwnerGamePlayer_GamePlayerIdAndPileAndFaceUp(
                        round.getRoundId(), player.getGamePlayerId(), GolfCard.Pile.GRID, false);

        if (faceDownCount > 0) {
            if (flipPosition == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "You must flip a face-down card when discarding from the stock pile");
            }
            if (flipPosition < 1 || flipPosition > 6) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "flipPosition must be 1–6");
            }
            GolfCard toFlip = golfCardRepository
                    .findByRound_RoundIdAndOwnerGamePlayer_GamePlayerIdAndPositionAndPile(
                            round.getRoundId(), player.getGamePlayerId(), flipPosition, GolfCard.Pile.GRID)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                            "No card at position " + flipPosition));
            if (toFlip.isFaceUp()) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "That card is already face-up; choose a face-down card to flip");
            }
            toFlip.setFaceUp(true);
            golfCardRepository.save(toFlip);
        }

        round.setCurrentDrawSource(null);
        roundRepository.save(round);

        checkAndAdvanceTurn(gameId, round, player);
    }


    private void checkAndAdvanceTurn(Long gameId, Round round, GamePlayer currentPlayer) {
        boolean allFaceUp = isAllFaceUp(round, currentPlayer);

        if (allFaceUp && round.getStatus() == Round.Status.ACTIVE) {
            round.setStatus(Round.Status.FINAL_TURNS);
            round.setFinalTurnTriggeredByGamePlayer(currentPlayer);
            roundRepository.save(round);
        }

        List<GamePlayer> players = gamePlayerRepository.findByGame_GameIdOrderBySeatNumberAsc(gameId);
        advanceTurn(gameId, round, players, currentPlayer);
    }

    private boolean isAllFaceUp(Round round, GamePlayer player) {
        long faceDown = golfCardRepository
                .countByRound_RoundIdAndOwnerGamePlayer_GamePlayerIdAndPileAndFaceUp(
                        round.getRoundId(), player.getGamePlayerId(), GolfCard.Pile.GRID, false);
        return faceDown == 0;
    }

    private void advanceTurn(Long gameId, Round round, List<GamePlayer> players, GamePlayer current) {
        int idx = -1;
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getGamePlayerId().equals(current.getGamePlayerId())) {
                idx = i;
                break;
            }
        }

        int nextIdx = (idx + 1) % players.size();
        GamePlayer nextPlayer = players.get(nextIdx);

        if (round.getStatus() == Round.Status.FINAL_TURNS
                && round.getFinalTurnTriggeredByGamePlayer() != null
                && nextPlayer.getGamePlayerId()
                        .equals(round.getFinalTurnTriggeredByGamePlayer().getGamePlayerId())) {
            endRound(gameId, round, players);
        } else {
            round.setCurrentTurnGamePlayer(nextPlayer);
            roundRepository.save(round);
        }
    }


    private void endRound(Long gameId, Round round, List<GamePlayer> players) {
        Long roundId = round.getRoundId();

        List<GolfCard> gridCards = golfCardRepository.findByRound_RoundIdAndPile(roundId, GolfCard.Pile.GRID);
        gridCards.forEach(c -> c.setFaceUp(true));
        golfCardRepository.saveAll(gridCards);

        List<GolfCard> handCards = golfCardRepository.findByRound_RoundIdAndPile(roundId, GolfCard.Pile.HAND);
        if (!handCards.isEmpty()) {
            int order = getNextDiscardOrder(round);
            for (GolfCard c : handCards) {
                c.setPile(GolfCard.Pile.DISCARD);
                c.setFaceUp(true);
                c.setPosition(null);
                c.setOwnerGamePlayer(null);
                c.setDrawOrder(order++);
            }
            golfCardRepository.saveAll(handCards);
        }

        List<RoundScore> scores = new ArrayList<>();
        for (GamePlayer player : players) {
            List<GolfCard> playerCards = gridCards.stream()
                    .filter(c -> c.getOwnerGamePlayer() != null
                            && c.getOwnerGamePlayer().getGamePlayerId().equals(player.getGamePlayerId()))
                    .sorted((a, b) -> Integer.compare(a.getPosition(), b.getPosition()))
                    .toList();

            int roundScore = calculateScore(playerCards);

            RoundScore rs = new RoundScore();
            rs.setRound(round);
            rs.setGamePlayer(player);
            rs.setScore(roundScore);
            scores.add(rs);

            int current = player.getTotalScore() == null ? 0 : player.getTotalScore();
            player.setTotalScore(current + roundScore);
        }
        roundScoreRepository.saveAll(scores);
        gamePlayerRepository.saveAll(players);

        round.setStatus(Round.Status.SCORED);
        round.setEndedAt(LocalDateTime.now());
        round.setCurrentTurnGamePlayer(null);
        roundRepository.save(round);

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Game not found"));

        boolean isTutorialGame = players.stream()
        .anyMatch(p -> "tutorial_bot".equals(p.getUser().getUsername()));

        if (isTutorialGame) {
            game.setStatus(Game.Status.COMPLETED);
            gameRepository.save(game);
            return;
        }

        if (round.getRoundNumber() >= 9) {
            game.setStatus(Game.Status.COMPLETED);
            gameRepository.save(game);
        } else {
            roundService.startRound(gameId);
        }
    }


    
    static int calculateScore(List<GolfCard> playerCards) {
        int total = 0;
        for (int col = 0; col < 3; col++) {
            GolfCard top    = findAtPosition(playerCards, col + 1);
            GolfCard bottom = findAtPosition(playerCards, col + 4);
            if (top != null && bottom != null && top.getRank() == bottom.getRank()) {
            } else {
                total += cardValue(top) + cardValue(bottom);
            }
        }
        return total;
    }

    private static GolfCard findAtPosition(List<GolfCard> cards, int pos) {
        return cards.stream()
                .filter(c -> c.getPosition() != null && c.getPosition() == pos)
                .findFirst().orElse(null);
    }

    private static int cardValue(GolfCard card) {
        if (card == null) return 0;
        return switch (card.getRank()) {
            case ACE   -> 1;
            case TWO   -> -2;
            case THREE -> 3;
            case FOUR  -> 4;
            case FIVE  -> 5;
            case SIX   -> 6;
            case SEVEN -> 7;
            case EIGHT -> 8;
            case NINE  -> 9;
            case TEN   -> 10;
            case JACK  -> 10;
            case QUEEN -> 10;
            case KING  -> 0;
        };
    }


    private int getNextDiscardOrder(Round round) {
        return golfCardRepository
                .findTopByRound_RoundIdAndPileOrderByDrawOrderDesc(round.getRoundId(), GolfCard.Pile.DISCARD)
                .map(c -> c.getDrawOrder() == null ? 1 : c.getDrawOrder() + 1)
                .orElse(1);
    }

    private GamePlayer getPlayer(Long gameId, Long userId) {
        return gamePlayerRepository.findByGame_GameIdAndUser_UserId(gameId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "You are not in this game"));
    }

    private Round getActiveRound(Long gameId, Round.Status... statuses) {
        Round round = roundRepository.findTopByGame_GameIdOrderByRoundNumberDesc(gameId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No active round found"));
        if (!Arrays.asList(statuses).contains(round.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Round is not in the correct state for this action (current: " + round.getStatus() + ")");
        }
        return round;
    }

    private void validateCurrentTurn(Round round, GamePlayer player) {
        if (round.getCurrentTurnGamePlayer() == null
                || !round.getCurrentTurnGamePlayer().getGamePlayerId().equals(player.getGamePlayerId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "It is not your turn");
        }
    }
}
