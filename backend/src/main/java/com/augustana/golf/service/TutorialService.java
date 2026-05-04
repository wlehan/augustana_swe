package com.augustana.golf.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.augustana.golf.domain.dto.GameStateResponse;
import com.augustana.golf.domain.dto.TutorialStateResponse;
import com.augustana.golf.domain.model.Game;
import com.augustana.golf.domain.model.GamePlayer;
import com.augustana.golf.domain.model.GolfCard;
import com.augustana.golf.domain.model.Round;
import com.augustana.golf.domain.model.RoundScore;
import com.augustana.golf.domain.model.TutorialStep;
import com.augustana.golf.domain.model.User;
import com.augustana.golf.exception.ApiException;
import com.augustana.golf.repository.GamePlayerRepository;
import com.augustana.golf.repository.GameRepository;
import com.augustana.golf.repository.GolfCardRepository;
import com.augustana.golf.repository.RoundRepository;
import com.augustana.golf.repository.RoundScoreRepository;
import com.augustana.golf.repository.UserRepository;

/**
 * Runs the single-player tutorial by wrapping a normal two-player game.
 *
 * <p>The human player is always seat 1 and the seeded {@code tutorial_bot}
 * account is seat 2. Tutorial steps are derived from the live game state
 * instead of being persisted, so refreshing the client does not need a separate
 * tutorial session record.</p>
 */
@Service
public class TutorialService {

    static final String BOT_USERNAME = "tutorial_bot";

    private final GameService gameService;
    private final RoundService roundService;
    private final GamePlayerRepository gamePlayerRepository;
    private final GolfCardRepository golfCardRepository;
    private final RoundRepository roundRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final RoundScoreRepository roundScoreRepository;

    private final Random rng = new Random();

    public TutorialService(
            GameService gameService,
            RoundService roundService,
            GameRepository gameRepository,
            GamePlayerRepository gamePlayerRepository,
            GolfCardRepository golfCardRepository,
            RoundRepository roundRepository,
            RoundScoreRepository roundScoreRepository,
            UserRepository userRepository) {
        this.gameService = gameService;
        this.roundService = roundService;
        this.gamePlayerRepository = gamePlayerRepository;
        this.golfCardRepository = golfCardRepository;
        this.roundRepository = roundRepository;
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
        this.roundScoreRepository = roundScoreRepository;
    }

    /**
     * Creates the tutorial game, joins the bot, starts round one, and returns
     * the opening guidance state for the human player.
     */
    @Transactional
    public TutorialStateResponse startTutorial(Long humanUserId) {
        var createdGame = gameService.createGame(humanUserId, 2);

        Game game = gameRepository.findById(createdGame.getGameId())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Tutorial game not found after creation"
                ));

        User bot = getBotUser();
        joinAsBot(game, bot);
        roundService.startRound(game.getGameId());

        GameStateResponse state = roundService.getGameState(game.getGameId(), humanUserId);
        return TutorialStateResponse.of(state, TutorialStep.WELCOME, 0, false, false);
    }

    /**
     * Rebuilds the tutorial wrapper around the current game state without
     * mutating the round.
     */
    @Transactional(readOnly = true)
    public TutorialStateResponse getCurrentState(Long gameId, Long humanUserId) {
        GameStateResponse state = roundService.getGameState(gameId, humanUserId);
        return buildTutorialState(state, gameId, humanUserId, false);
    }

    /**
     * Makes the bot complete its two setup flips after the human has finished
     * theirs.
     */
    @Transactional
    public TutorialStateResponse botFlipInitial(Long gameId, Long humanUserId) {
        GamePlayer bot = getBotPlayer(gameId);
        Round round = getActiveRound(gameId);

        List<GolfCard> botGrid = golfCardRepository
                .findByRound_RoundIdAndOwnerGamePlayer_GamePlayerIdAndPile(
                        round.getRoundId(), bot.getGamePlayerId(), GolfCard.Pile.GRID);

        long alreadyFlipped = botGrid.stream().filter(GolfCard::isFaceUp).count();

        List<GolfCard> faceDown = botGrid.stream()
                .filter(c -> !c.isFaceUp())
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        Collections.shuffle(faceDown);

        int toFlip = Math.min(2 - (int) alreadyFlipped, faceDown.size());

        for (int i = 0; i < toFlip; i++) {
            GolfCard card = faceDown.get(i);
            card.setFaceUp(true);
            golfCardRepository.save(card);
        }

        boolean everyoneReady = gamePlayerRepository
                .findByGame_GameIdOrderBySeatNumberAsc(gameId)
                .stream()
                .allMatch(p -> golfCardRepository
                        .countByRound_RoundIdAndOwnerGamePlayer_GamePlayerIdAndPileAndFaceUp(
                                round.getRoundId(),
                                p.getGamePlayerId(),
                                GolfCard.Pile.GRID,
                                true
                        ) >= 2);

        if (everyoneReady && round.getStatus() == Round.Status.SETUP) {
            round.setStatus(Round.Status.ACTIVE);
            roundRepository.save(round);
        }

        GameStateResponse state = roundService.getGameState(gameId, humanUserId);
        return buildTutorialState(state, gameId, humanUserId, true);
    }

    /**
     * Executes one legal random bot turn so the tutorial can demonstrate the
     * full draw, resolve, and advance-turn loop.
     */
    @Transactional
    public TutorialStateResponse executeBotTurn(Long gameId, Long humanUserId) {
        Round round = getActiveRound(gameId);
        GamePlayer bot = getBotPlayer(gameId);

        assertBotTurn(round, bot);

        boolean drawFromDiscard = rng.nextBoolean();
        GolfCard drawnCard = drawFromDiscard
                ? drawFromDiscard(round, bot)
                : drawFromDeck(round, bot);

        if (drawnCard == null) {
            drawnCard = drawFromDiscard(round, bot);
        }

        if (drawnCard == null) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "No cards available to draw");
        }

        List<GolfCard> botGrid = golfCardRepository
                .findByRound_RoundIdAndOwnerGamePlayer_GamePlayerIdAndPile(
                        round.getRoundId(), bot.getGamePlayerId(), GolfCard.Pile.GRID);

        boolean swap = rng.nextBoolean();

        if (swap && !botGrid.isEmpty()) {
            GolfCard target = botGrid.get(rng.nextInt(botGrid.size()));
            swapCards(drawnCard, target, round);
        } else {
            discardCard(drawnCard, round);
            List<GolfCard> faceDown = botGrid.stream().filter(c -> !c.isFaceUp()).toList();
            if (!faceDown.isEmpty()) {
                GolfCard toFlip = faceDown.get(rng.nextInt(faceDown.size()));
                toFlip.setFaceUp(true);
                golfCardRepository.save(toFlip);
            }
        }

        checkAndTriggerFinalTurns(round, bot);
        advanceTurn(round, gameId);

        GameStateResponse state = roundService.getGameState(gameId, humanUserId);
        return buildTutorialState(state, gameId, humanUserId, true);
    }

    private TutorialStateResponse buildTutorialState(
            GameStateResponse state,
            Long gameId,
            Long humanUserId,
            boolean botJustMoved) {

        int humanFlips = humanFlipsCompleted(state, humanUserId);
        boolean allReady = allPlayersReady(state);
        TutorialStep step = deriveStep(state, humanFlips, allReady);

        return TutorialStateResponse.of(state, step, humanFlips, allReady, botJustMoved);
    }

    /**
     * Maps the live round state to a tutorial step. Terminal and setup states
     * are checked before active-turn prompts because they take priority in the
     * UI.
     */
    private TutorialStep deriveStep(GameStateResponse state, int humanFlips, boolean allReady) {
        String roundStatus = state.round == null ? "UNKNOWN" : state.round.status;

        if ("SCORED".equals(roundStatus)) {
            return TutorialStep.TUTORIAL_COMPLETE;
        }

        if ("SETUP".equals(roundStatus)) {
            if (humanFlips == 0) return TutorialStep.FLIP_FIRST;
            if (humanFlips == 1) return TutorialStep.FLIP_SECOND;
            return TutorialStep.WAIT_FOR_OTHERS_TO_FLIP;
        }

        if (state.round != null && state.round.currentTurnUserId != null
                && ("ACTIVE".equals(roundStatus) || "FINAL_TURNS".equals(roundStatus))) {

            boolean isHumanTurn = state.round.currentTurnUserId.equals(humanPlayerUserId(state));

            if (!isHumanTurn) {
                return TutorialStep.BOT_TURN;
            }

            GameStateResponse.PlayerBoardView humanBoard = humanBoard(state);
            if (humanBoard != null && humanBoard.heldCard != null) {
                return TutorialStep.YOUR_TURN_DECIDE;
            }

            if ("FINAL_TURNS".equals(roundStatus)) {
                return TutorialStep.FINAL_TURNS;
            }

            return TutorialStep.YOUR_TURN_DRAW;
        }

        if (state.round != null && state.round.currentTurnUserId != null) {
            boolean isHumanTurn = state.round.currentTurnUserId.equals(humanPlayerUserId(state));

            if (!isHumanTurn) {
                return TutorialStep.BOT_TURN;
            }

            GameStateResponse.PlayerBoardView humanBoard = humanBoard(state);
            if (humanBoard != null && humanBoard.heldCard != null) {
                return TutorialStep.YOUR_TURN_DECIDE;
            }

            return TutorialStep.YOUR_TURN_DRAW;
        }

        return TutorialStep.YOUR_TURN_DRAW;
    }

    private GolfCard drawFromDeck(Round round, GamePlayer player) {
        return golfCardRepository
                .findByRound_RoundIdAndPileOrderByDrawOrderAsc(round.getRoundId(), GolfCard.Pile.DRAW)
                .stream()
                .findFirst()
                .map(card -> {
                    card.setOwnerGamePlayer(player);
                    card.setPile(GolfCard.Pile.HAND);
                    card.setFaceUp(true);
                    round.setCurrentDrawSource("STOCK");
                    roundRepository.save(round);
                    return golfCardRepository.save(card);
                })
                .orElse(null);
    }

    private GolfCard drawFromDiscard(Round round, GamePlayer player) {
        return golfCardRepository
                .findTopByRound_RoundIdAndPileOrderByDrawOrderDesc(round.getRoundId(), GolfCard.Pile.DISCARD)
                .map(card -> {
                    card.setOwnerGamePlayer(player);
                    card.setPile(GolfCard.Pile.HAND);
                    card.setFaceUp(true);
                    round.setCurrentDrawSource("DISCARD");
                    roundRepository.save(round);
                    return golfCardRepository.save(card);
                })
                .orElse(null);
    }

    private void swapCards(GolfCard heldCard, GolfCard gridCard, Round round) {
        int position = gridCard.getPosition();
        gridCard.setPile(GolfCard.Pile.DISCARD);
        gridCard.setFaceUp(true);
        gridCard.setPosition(null);
        gridCard.setDrawOrder(nextDiscardOrder(round));
        golfCardRepository.save(gridCard);

        heldCard.setPile(GolfCard.Pile.GRID);
        heldCard.setPosition(position);
        heldCard.setFaceUp(true);
        heldCard.setDrawOrder(null);
        golfCardRepository.save(heldCard);
    }

    private void discardCard(GolfCard heldCard, Round round) {
        heldCard.setPile(GolfCard.Pile.DISCARD);
        heldCard.setFaceUp(true);
        heldCard.setPosition(null);
        heldCard.setDrawOrder(nextDiscardOrder(round));
        golfCardRepository.save(heldCard);
    }

    private int nextDiscardOrder(Round round) {
        return golfCardRepository
                .findTopByRound_RoundIdAndPileOrderByDrawOrderDesc(round.getRoundId(), GolfCard.Pile.DISCARD)
                .map(c -> (c.getDrawOrder() == null ? 0 : c.getDrawOrder()) + 1)
                .orElse(1);
    }

    private void checkAndTriggerFinalTurns(Round round, GamePlayer mover) {
        if (round.getStatus() != Round.Status.ACTIVE) return;

        List<GolfCard> grid = golfCardRepository
                .findByRound_RoundIdAndOwnerGamePlayer_GamePlayerIdAndPile(
                        round.getRoundId(), mover.getGamePlayerId(), GolfCard.Pile.GRID);

        boolean allFaceUp = !grid.isEmpty() && grid.stream().allMatch(GolfCard::isFaceUp);
        if (allFaceUp) {
            round.setStatus(Round.Status.FINAL_TURNS);
            round.setFinalTurnTriggeredByGamePlayer(mover);
            roundRepository.save(round);
        }
    }

    /**
     * Moves to the next seat. In final turns, reaching the player who triggered
     * the final-turn loop ends the tutorial round.
     */
    private void advanceTurn(Round round, Long gameId) {
        List<GamePlayer> players = gamePlayerRepository.findByGame_GameIdOrderBySeatNumberAsc(gameId);

        if (players.size() < 2) return;

        GamePlayer current = round.getCurrentTurnGamePlayer();
        int currentSeat = current == null ? 1 : current.getSeatNumber();

        GamePlayer next = players.stream()
                .filter(p -> p.getSeatNumber() > currentSeat)
                .findFirst()
                .orElse(players.get(0));

        if (round.getStatus() == Round.Status.FINAL_TURNS
                && round.getFinalTurnTriggeredByGamePlayer() != null
                && next.getGamePlayerId().equals(
                        round.getFinalTurnTriggeredByGamePlayer().getGamePlayerId()
                )) {
            endTutorialRound(gameId, round, players);
            return;
        }

        round.setCurrentTurnGamePlayer(next);
        roundRepository.save(round);
    }

    private void endTutorialRound(Long gameId, Round round, List<GamePlayer> players) {
        List<GolfCard> gridCards = golfCardRepository.findByRound_RoundIdAndPile(
                round.getRoundId(),
                GolfCard.Pile.GRID
        );

        gridCards.forEach(c -> c.setFaceUp(true));
        golfCardRepository.saveAll(gridCards);

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

            player.setTotalScore(roundScore);
        }

        roundScoreRepository.saveAll(scores);
        gamePlayerRepository.saveAll(players);

        round.setStatus(Round.Status.SCORED);
        round.setCurrentTurnGamePlayer(null);
        round.setEndedAt(LocalDateTime.now());
        roundRepository.save(round);

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Game not found"));

        game.setStatus(Game.Status.COMPLETED);
        gameRepository.save(game);
    }

    /**
     * Scores a six-card grid using the same column-cancel rules as normal play.
     */
    static int calculateScore(List<GolfCard> playerCards) {
        int total = 0;
        for (int col = 0; col < 3; col++) {
            GolfCard top = findAtPosition(playerCards, col + 1);
            GolfCard bottom = findAtPosition(playerCards, col + 4);
            if (!(top != null && bottom != null && top.getRank() == bottom.getRank())) {
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
            case ACE -> 1;
            case TWO -> -2;
            case THREE -> 3;
            case FOUR -> 4;
            case FIVE -> 5;
            case SIX -> 6;
            case SEVEN -> 7;
            case EIGHT -> 8;
            case NINE -> 9;
            case TEN -> 10;
            case JACK -> 10;
            case QUEEN -> 10;
            case KING -> 0;
        };
    }

    private int humanFlipsCompleted(GameStateResponse state, Long humanUserId) {
        if (state.players == null) return 0;
        return state.players.stream()
                .filter(p -> p.userId != null && p.userId.equals(humanUserId))
                .mapToInt(p -> p.initialFlipsCount)
                .findFirst()
                .orElse(0);
    }

    private boolean allPlayersReady(GameStateResponse state) {
        if (state.players == null || state.players.isEmpty()) return false;
        return state.players.stream().allMatch(p -> p.initialFlipsCount >= 2);
    }

    private Long humanPlayerUserId(GameStateResponse state) {
        if (state.players == null || state.players.isEmpty()) return null;
        return state.players.stream()
                .filter(p -> p.seatNumber == 1)
                .map(p -> p.userId)
                .findFirst()
                .orElse(null);
    }

    private GameStateResponse.PlayerBoardView humanBoard(GameStateResponse state) {
        if (state.players == null) return null;
        return state.players.stream()
                .filter(p -> p.seatNumber == 1)
                .findFirst()
                .orElse(null);
    }

    private User getBotUser() {
        return userRepository.findByUsername(BOT_USERNAME)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Tutorial bot user not found in database"
                ));
    }

    private void joinAsBot(Game game, User bot) {
        GamePlayer gp = new GamePlayer();
        gp.setGame(game);
        gp.setUser(bot);
        gp.setSeatNumber(2);
        gp.setTotalScore(0);
        gamePlayerRepository.save(gp);
    }

    private GamePlayer getBotPlayer(Long gameId) {
        return gamePlayerRepository
                .findByGame_GameIdOrderBySeatNumberAsc(gameId)
                .stream()
                .filter(p -> BOT_USERNAME.equals(p.getUser().getUsername()))
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Bot player not found"));
    }

    private Round getActiveRound(Long gameId) {
        return roundRepository
                .findTopByGame_GameIdOrderByRoundNumberDesc(gameId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No active round"));
    }

    private void assertBotTurn(Round round, GamePlayer bot) {
        GamePlayer current = round.getCurrentTurnGamePlayer();
        if (current == null || !current.getGamePlayerId().equals(bot.getGamePlayerId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "It is not the bot's turn");
        }
    }
}
