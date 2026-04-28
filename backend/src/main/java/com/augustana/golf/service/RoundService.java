package com.augustana.golf.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.augustana.golf.domain.dto.GameStateResponse;
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
public class RoundService {

    private final GameRepository gameRepository;
    private final GamePlayerRepository gamePlayerRepository;
    private final RoundRepository roundRepository;
    private final GolfCardRepository golfCardRepository;
    private final RoundScoreRepository roundScoreRepository;

    public RoundService(
            GameRepository gameRepository,
            GamePlayerRepository gamePlayerRepository,
            RoundRepository roundRepository,
            GolfCardRepository golfCardRepository,
            RoundScoreRepository roundScoreRepository) {
        this.gameRepository = gameRepository;
        this.gamePlayerRepository = gamePlayerRepository;
        this.roundRepository = roundRepository;
        this.golfCardRepository = golfCardRepository;
        this.roundScoreRepository = roundScoreRepository;
    }

    @Transactional
    public Round startRound(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Game not found"));

        List<GamePlayer> players = gamePlayerRepository.findByGame_GameIdOrderBySeatNumberAsc(gameId);

        if (players.size() < 2) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "At least 2 players are required to start a round");
        }

        if (players.size() > 4) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "No more than 4 players can start a round");
        }

        boolean activeRoundExists = !roundRepository.findByGame_GameIdAndStatusIn(
                gameId,
                List.of(Round.Status.SETUP, Round.Status.ACTIVE, Round.Status.FINAL_TURNS)).isEmpty();

        if (activeRoundExists) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "A round is already in progress");
        }

        int nextRoundNumber = roundRepository
                .findTopByGame_GameIdOrderByRoundNumberDesc(gameId)
                .map(r -> r.getRoundNumber() + 1)
                .orElse(1);

        Round round = new Round();
        round.setGame(game);
        round.setRoundNumber(nextRoundNumber);
        round.setStatus(Round.Status.SETUP);
        round.setDealerSeat(1);
        round.setCurrentTurnGamePlayer(players.get(0));

        Round savedRound = roundRepository.save(round);

        List<GolfCard> deck = buildShuffledDeck();
        List<GolfCard> cardsToSave = new ArrayList<>();

        int deckIndex = 0;

        for (GamePlayer player : players) {
            for (int position = 1; position <= 6; position++) {
                GolfCard card = deck.get(deckIndex++);
                card.setRound(savedRound);
                card.setOwnerGamePlayer(player);
                card.setPosition(position);
                card.setFaceUp(false);
                card.setPile(GolfCard.Pile.GRID);
                card.setDrawOrder(null);
                cardsToSave.add(card);
            }
        }

        int drawOrder = 1;
        while (deckIndex < deck.size()) {
            GolfCard card = deck.get(deckIndex++);
            card.setRound(savedRound);
            card.setOwnerGamePlayer(null);
            card.setPosition(null);
            card.setFaceUp(false);
            card.setPile(GolfCard.Pile.DRAW);
            card.setDrawOrder(drawOrder++);
            cardsToSave.add(card);
        }

        GolfCard topDraw = null;
        for (GolfCard card : cardsToSave) {
            if (card.getPile() == GolfCard.Pile.DRAW && card.getDrawOrder() == 1) {
                topDraw = card;
                break;
            }
        }

        if (topDraw == null) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not initialize discard pile");
        }

        topDraw.setPile(GolfCard.Pile.DISCARD);
        topDraw.setFaceUp(true);
        topDraw.setDrawOrder(1);

        for (GolfCard card : cardsToSave) {
            if (card.getPile() == GolfCard.Pile.DRAW && card.getDrawOrder() != null) {
                card.setDrawOrder(card.getDrawOrder() - 1);
            }
        }

        golfCardRepository.saveAll(cardsToSave);

        game.setStatus(Game.Status.IN_PROGRESS);
        game.setCurrentRound(savedRound.getRoundNumber());
        gameRepository.save(game);

        return savedRound;
    }

    @Transactional(readOnly = true)
    public GameStateResponse getGameState(Long gameId, Long requestingUserId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Game not found"));

        Round round = roundRepository.findTopByGame_GameIdOrderByRoundNumberDesc(gameId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No round found for game"));

        List<GamePlayer> players = gamePlayerRepository.findByGame_GameIdOrderBySeatNumberAsc(gameId);
        List<GolfCard> allCards = golfCardRepository.findByRound_RoundId(round.getRoundId());

        GameStateResponse response = new GameStateResponse();
        response.gameId = game.getGameId();
        response.gameCode = game.getGameCode();
        response.gameStatus = game.getStatus().name();
        response.currentRound = game.getCurrentRound();

        GameStateResponse.RoundView roundView = new GameStateResponse.RoundView();
        roundView.roundId = round.getRoundId();
        roundView.status = round.getStatus().name();
        roundView.currentDrawSource = round.getCurrentDrawSource();

        if (round.getCurrentTurnGamePlayer() != null) {
            roundView.currentTurnGamePlayerId = round.getCurrentTurnGamePlayer().getGamePlayerId();
            if (round.getCurrentTurnGamePlayer().getUser() != null) {
                roundView.currentTurnUserId = round.getCurrentTurnGamePlayer().getUser().getUserId();
            }
        }

        if (round.getFinalTurnTriggeredByGamePlayer() != null) {
            roundView.finalTurnTriggeredByGamePlayerId =
                    round.getFinalTurnTriggeredByGamePlayer().getGamePlayerId();
        }

        GolfCard discardTop = allCards.stream()
                .filter(c -> c.getPile() == GolfCard.Pile.DISCARD)
                .max((a, b) -> {
                    int ao = a.getDrawOrder() == null ? Integer.MIN_VALUE : a.getDrawOrder();
                    int bo = b.getDrawOrder() == null ? Integer.MIN_VALUE : b.getDrawOrder();
                    return Integer.compare(ao, bo);
                })
                .orElse(null);

        roundView.drawPileCount = (int) allCards.stream()
                .filter(c -> c.getPile() == GolfCard.Pile.DRAW).count();
        roundView.discardTop = toVisibleCard(discardTop);

        response.round = roundView;

        List<RoundScore> allScores = roundScoreRepository.findByRound_Game_GameIdOrderByRound_RoundNumberAsc(gameId);
        Map<Long, List<RoundScore>> scoresByRound = allScores.stream()
                .collect(Collectors.groupingBy(rs -> rs.getRound().getRoundId()));

        List<Round> allRounds = roundRepository.findByGame_GameIdOrderByRoundNumberAsc(gameId);
        response.allRoundScores = allRounds.stream()
                .filter(r -> !scoresByRound.getOrDefault(r.getRoundId(), List.of()).isEmpty())
                .map(r -> {
                    GameStateResponse.RoundScoreSummary summary = new GameStateResponse.RoundScoreSummary();
                    summary.roundNumber = r.getRoundNumber();
                    summary.perPlayerScores = scoresByRound.get(r.getRoundId()).stream()
                            .map(rs -> {
                                GameStateResponse.PerPlayerRoundScore prs = new GameStateResponse.PerPlayerRoundScore();
                                prs.gamePlayerId = rs.getGamePlayer().getGamePlayerId();
                                prs.score = rs.getScore();
                                return prs;
                            })
                            .toList();
                    return summary;
                })
                .toList();

        Map<Long, Integer> currentRoundScoreMap = roundScoreRepository
                .findByRound_RoundId(round.getRoundId()).stream()
                .collect(Collectors.toMap(
                        rs -> rs.getGamePlayer().getGamePlayerId(),
                        RoundScore::getScore));

        response.players = players.stream().map(player -> {
            GameStateResponse.PlayerBoardView view = new GameStateResponse.PlayerBoardView();
            view.userId = player.getUser().getUserId();
            view.username = player.getUser().getUsername();
            view.gamePlayerId = player.getGamePlayerId();
            view.seatNumber = player.getSeatNumber();
            view.totalScore = player.getTotalScore();
            view.roundScore = currentRoundScoreMap.getOrDefault(player.getGamePlayerId(), null);

            view.initialFlipsCount = (int) allCards.stream()
                    .filter(c -> c.getPile() == GolfCard.Pile.GRID
                            && c.getOwnerGamePlayer() != null
                            && c.getOwnerGamePlayer().getGamePlayerId().equals(player.getGamePlayerId())
                            && c.isFaceUp())
                    .count();

            boolean isRequestingPlayer = requestingUserId != null
                    && requestingUserId.equals(player.getUser().getUserId());

            if (isRequestingPlayer) {
                view.heldCard = allCards.stream()
                        .filter(c -> c.getPile() == GolfCard.Pile.HAND
                                && c.getOwnerGamePlayer() != null
                                && c.getOwnerGamePlayer().getGamePlayerId().equals(player.getGamePlayerId()))
                        .findFirst()
                        .map(this::toVisibleCard)
                        .orElse(null);
            }

            view.cards = allCards.stream()
                    .filter(c -> c.getPile() == GolfCard.Pile.GRID
                            && c.getOwnerGamePlayer() != null
                            && c.getOwnerGamePlayer().getGamePlayerId().equals(player.getGamePlayerId()))
                    .sorted((a, b) -> Integer.compare(a.getPosition(), b.getPosition()))
                    .map(c -> toGridCardForViewer(c, requestingUserId))
                    .toList();

            return view;
        }).toList();

        return response;
    }

    private GameStateResponse.CardView toVisibleCard(GolfCard card) {
        if (card == null) return null;
        GameStateResponse.CardView view = new GameStateResponse.CardView();
        view.position = card.getPosition();
        view.faceUp = card.isFaceUp();
        view.revealedToViewer = true;
        view.suit = card.getSuit() == null ? null : card.getSuit().name();
        view.rank = card.getRank() == null ? null : card.getRank().name();
        return view;
    }

    private GameStateResponse.CardView toGridCardForViewer(GolfCard card, Long requestingUserId) {
        if (card == null) return null;

        GameStateResponse.CardView view = new GameStateResponse.CardView();
        view.position = card.getPosition();
        view.faceUp = card.isFaceUp();
        view.revealedToViewer = card.isFaceUp();
        view.suit = view.revealedToViewer && card.getSuit() != null ? card.getSuit().name() : null;
        view.rank = view.revealedToViewer && card.getRank() != null ? card.getRank().name() : null;
        return view;
    }

    private List<GolfCard> buildShuffledDeck() {
        List<GolfCard> deck = new ArrayList<>();
        for (GolfCard.Suit suit : GolfCard.Suit.values()) {
            for (GolfCard.Rank rank : GolfCard.Rank.values()) {
                GolfCard card = new GolfCard();
                card.setSuit(suit);
                card.setRank(rank);
                deck.add(card);
            }
        }
        Collections.shuffle(deck);
        return deck;
    }
}
