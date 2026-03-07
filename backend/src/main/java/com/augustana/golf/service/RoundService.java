package com.augustana.golf.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.augustana.golf.domain.model.Game;
import com.augustana.golf.domain.model.GamePlayer;
import com.augustana.golf.domain.model.GolfCard;
import com.augustana.golf.domain.model.Round;
import com.augustana.golf.exception.ApiException;
import com.augustana.golf.repository.GamePlayerRepository;
import com.augustana.golf.repository.GameRepository;
import com.augustana.golf.repository.GolfCardRepository;
import com.augustana.golf.repository.RoundRepository;

@Service
public class RoundService {

    private final GameRepository gameRepository;
    private final GamePlayerRepository gamePlayerRepository;
    private final RoundRepository roundRepository;
    private final GolfCardRepository golfCardRepository;

    public RoundService(
        GameRepository gameRepository,
        GamePlayerRepository gamePlayerRepository,
        RoundRepository roundRepository,
        GolfCardRepository golfCardRepository
    ) {
        this.gameRepository = gameRepository;
        this.gamePlayerRepository = gamePlayerRepository;
        this.roundRepository = roundRepository;
        this.golfCardRepository = golfCardRepository;
    }

    @Transactional
    public Round startRound(Long gameId) {
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Game not found"));

        List<GamePlayer> players = gamePlayerRepository.findByGame_GameIdOrderBySeatNumberAsc(gameId);

        if (players.size() < 2) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "At least 2 players are required to start a round");
        }

        boolean activeRoundExists = !roundRepository.findByGame_GameIdAndStatusIn(
            gameId,
            List.of(Round.Status.SETUP, Round.Status.ACTIVE, Round.Status.FINAL_TURNS)
        ).isEmpty();

        if (activeRoundExists) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "A round is already in progress");
        }

        int nextRoundNumber = roundRepository
            .findTopByGame_GameIdOrderByRoundNumberDesc(gameId)
            .map(r -> r.getRoundNumber() + 1)
            .orElse(1);

        // Sets the host to be the first player in the list - this is arbitrary; can be changed later if desire
        Round round = new Round();
        round.setGame(game);
        round.setRoundNumber(nextRoundNumber);
        round.setStatus(Round.Status.ACTIVE);
        round.setDealerSeat(1);
        round.setCurrentTurnGamePlayer(players.get(0));

        Round savedRound = roundRepository.save(round);

        List<GolfCard> deck = buildShuffledDeck();
        List<GolfCard> cardsToSave = new ArrayList<>();

        int deckIndex = 0;

        // Deal 6 cards to each player into positions 1..6
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

        // Remaining cards go to draw pile
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

        // Flip top draw card into discard pile
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

        // Shift remaining draw pile order down by 1
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