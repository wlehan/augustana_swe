import React, { useEffect, useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import './Gamepage.css';
import gearIcon from '../assets/Icon.png';
import profileIcon from '../assets/profile.png';
import cardBack from '../assets/golfball.png';
import { getGame, startGame } from '../services/gameApi';

import aceclubs from '../assets/cards/clubs/aceclubs.png';
import twoclubs from '../assets/cards/clubs/2clubs.png';
import threeclubs from '../assets/cards/clubs/3clubs.png';
import fourclubs from '../assets/cards/clubs/4clubs.png';
import fiveclubs from '../assets/cards/clubs/5clubs.png';
import sixclubs from '../assets/cards/clubs/6clubs.png';
import sevenclubs from '../assets/cards/clubs/7clubs.png';
import eightclubs from '../assets/cards/clubs/8clubs.png';
import nineclubs from '../assets/cards/clubs/9clubs.png';
import tenclubs from '../assets/cards/clubs/10clubs.png';
import jackclubs from '../assets/cards/clubs/jackclubs.png';
import queenclubs from '../assets/cards/clubs/queenclubs.png';
import kingclubs from '../assets/cards/clubs/kingclubs.png';

import acediamonds from '../assets/cards/diamonds/acediamonds.png';
import twodiamonds from '../assets/cards/diamonds/2diamonds.png';
import threediamonds from '../assets/cards/diamonds/3diamonds.png';
import fourdiamonds from '../assets/cards/diamonds/4diamonds.png';
import fivediamonds from '../assets/cards/diamonds/5diamonds.png';
import sixdiamonds from '../assets/cards/diamonds/6diamonds.png';
import sevendiamonds from '../assets/cards/diamonds/7diamonds.png';
import eightdiamonds from '../assets/cards/diamonds/8diamonds.png';
import ninediamonds from '../assets/cards/diamonds/9diamonds.png';
import tendiamonds from '../assets/cards/diamonds/10diamonds.png';
import jackdiamonds from '../assets/cards/diamonds/jackdiamonds.png';
import queendiamonds from '../assets/cards/diamonds/queendiamonds.png';
import kingdiamonds from '../assets/cards/diamonds/kingdiamonds.png';

import acehearts from '../assets/cards/hearts/acehearts.png';
import twohearts from '../assets/cards/hearts/2hearts.png';
import threehearts from '../assets/cards/hearts/3hearts.png';
import fourhearts from '../assets/cards/hearts/4hearts.png';
import fivehearts from '../assets/cards/hearts/5hearts.png';
import sixhearts from '../assets/cards/hearts/6hearts.png';
import sevenhearts from '../assets/cards/hearts/7hearts.png';
import eighthearts from '../assets/cards/hearts/8hearts.png';
import ninehearts from '../assets/cards/hearts/9hearts.png';
import tenhearts from '../assets/cards/hearts/10hearts.png';
import jackhearts from '../assets/cards/hearts/jackhearts.png';
import queenhearts from '../assets/cards/hearts/queenhearts.png';
import kinghearts from '../assets/cards/hearts/kinghearts.png';

import acespades from '../assets/cards/spades/acespades.png';
import twospades from '../assets/cards/spades/2spades.png';
import threespades from '../assets/cards/spades/3spades.png';
import fourspades from '../assets/cards/spades/4spades.png';
import fivespades from '../assets/cards/spades/5spades.png';
import sixspades from '../assets/cards/spades/6spades.png';
import sevenspades from '../assets/cards/spades/7spades.png';
import eightspades from '../assets/cards/spades/8spades.png';
import ninespades from '../assets/cards/spades/9spades.png';
import tenspades from '../assets/cards/spades/10spades.png';
import jackspades from '../assets/cards/spades/jackspades.png';
import queenspades from '../assets/cards/spades/queenspades.png';
import kingspades from '../assets/cards/spades/kingspades.png';

const CARD_IMAGES = {
  CLUBS: {
    ACE: aceclubs,
    TWO: twoclubs,
    THREE: threeclubs,
    FOUR: fourclubs,
    FIVE: fiveclubs,
    SIX: sixclubs,
    SEVEN: sevenclubs,
    EIGHT: eightclubs,
    NINE: nineclubs,
    TEN: tenclubs,
    JACK: jackclubs,
    QUEEN: queenclubs,
    KING: kingclubs,
  },
  DIAMONDS: {
    ACE: acediamonds,
    TWO: twodiamonds,
    THREE: threediamonds,
    FOUR: fourdiamonds,
    FIVE: fivediamonds,
    SIX: sixdiamonds,
    SEVEN: sevendiamonds,
    EIGHT: eightdiamonds,
    NINE: ninediamonds,
    TEN: tendiamonds,
    JACK: jackdiamonds,
    QUEEN: queendiamonds,
    KING: kingdiamonds,
  },
  HEARTS: {
    ACE: acehearts,
    TWO: twohearts,
    THREE: threehearts,
    FOUR: fourhearts,
    FIVE: fivehearts,
    SIX: sixhearts,
    SEVEN: sevenhearts,
    EIGHT: eighthearts,
    NINE: ninehearts,
    TEN: tenhearts,
    JACK: jackhearts,
    QUEEN: queenhearts,
    KING: kinghearts,
  },
  SPADES: {
    ACE: acespades,
    TWO: twospades,
    THREE: threespades,
    FOUR: fourspades,
    FIVE: fivespades,
    SIX: sixspades,
    SEVEN: sevenspades,
    EIGHT: eightspades,
    NINE: ninespades,
    TEN: tenspades,
    JACK: jackspades,
    QUEEN: queenspades,
    KING: kingspades,
  },
};

function getCardImage(card) {
  if (!card) return null;
  if (!card.faceUp) return cardBack;
  return CARD_IMAGES?.[card.suit]?.[card.rank] || null;
}

function getCardAlt(card) {
  if (!card) return 'Card slot';
  if (!card.faceUp) return 'Face-down card';
  return `${card.rank} of ${card.suit}`;
}

function PlayerHand({ position, playerMeta }) {
  const slots = [1, 2, 3, 4, 5, 6];
  const cardsByPosition = new Map(
    (playerMeta?.cards || []).map((card) => [card.position, card])
  );

  return (
    <div className={`player-area ${position}`}>
      {playerMeta && (
        <div className={`score-chip ${position}`}>
          <span className="score-chip-name">{playerMeta.name}</span>
          <span className="score-chip-total">{playerMeta.total}</span>
        </div>
      )}

      {position !== 'bottom' && (
        <img src={profileIcon} className="player-profile-img" alt="player" />
      )}

      <div className="card-grid">
        {slots.map((slot, index) => {
          const card = cardsByPosition.get(slot);
          const imageSrc = getCardImage(card);

          return (
            <div
              key={slot}
              className={`card-slot ${card ? 'dealt-card' : ''}`}
              style={card ? { animationDelay: `${index * 90}ms` } : undefined}
            >
              {imageSrc && (
                <img
                  src={imageSrc}
                  alt={getCardAlt(card)}
                  className="playing-card-img"
                />
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default function GamePage() {
  const [params] = useSearchParams();
  const gameId = params.get('gameId');

  const [game, setGame] = useState(null);
  const [errorMsg, setErrorMsg] = useState('');
  const [isLedgerOpen, setIsLedgerOpen] = useState(false);
  const [startingGame, setStartingGame] = useState(false);
  const [startError, setStartError] = useState('');
  const [copyNotice, setCopyNotice] = useState('');

  const user = useMemo(() => {
    const raw = localStorage.getItem('demo_user');
    if (!raw) return null;
    try {
      return JSON.parse(raw);
    } catch {
      return null;
    }
  }, []);

  const effectiveStatus = game?.status ?? game?.gameStatus ?? null;


  /*For a more complete solution, you’ll probably want a new frontend API function like getGameState({ gameId }) that calls:

GET /api/games/{gameId}/state

Then after the game starts, you can poll /state instead of /games/{gameId}.

But for now, the fix above should make the dealt cards appear.*/
  useEffect(() => {
  if (!gameId) {
    setErrorMsg('Missing gameId in URL.');
    return;
  }

  // Once the game has started, do not call getGame() anymore,
  // because it returns the simpler lobby response and overwrites
  // the richer start/state response that contains cards.
  if (effectiveStatus && effectiveStatus !== 'WAITING') {
    return;
  }

  let cancelled = false;

  const loadGame = async () => {
    try {
      const data = await getGame({ gameId });
      if (!cancelled) {
        setGame(data);
        setErrorMsg('');
      }
    } catch (e) {
      if (!cancelled) {
        setErrorMsg(
          e?.response?.data?.message ||
            e?.response?.data?.error ||
            'Could not load game.'
        );
      }
    }
  };

  loadGame();
  const intervalId = setInterval(loadGame, 2000);

  return () => {
    cancelled = true;
    clearInterval(intervalId);
  };
}, [gameId, effectiveStatus]);

  const onCopyCode = async () => {
    if (!game?.gameCode) return;
    try {
      await navigator.clipboard.writeText(game.gameCode);
      setCopyNotice('Copied to clipboard');
    } catch {
      window.prompt('Copy this code:', game.gameCode);
      setCopyNotice('Copy the code from the prompt');
    }

    window.setTimeout(() => {
      setCopyNotice('');
    }, 1600);
  };

  const onStartGame = async () => {
    if (!gameId || startingGame) return;

    setStartError('');
    setStartingGame(true);

    try {
      const data = await startGame({ gameId, userId: user?.userId });
      setGame(data);
      setErrorMsg('');
    } catch (e) {
      console.error('Start game failed:', e?.response?.data || e);
      setStartError(
        e?.response?.data?.message ||
          e?.response?.data?.error ||
          `Could not start game (${e?.response?.status || 'network error'}).`
      );
    } finally {
      setStartingGame(false);
    }
  };

  const playerScores =
    Array.isArray(game?.players) && game.players.length > 0
      ? [...game.players]
          .sort((a, b) => (a?.seatNumber || 0) - (b?.seatNumber || 0))
          .map((player, index) => ({
            id: player?.gamePlayerId || player?.userId || `${index}`,
            userId: player?.userId || null,
            name:
              player?.displayName ||
              player?.username ||
              player?.name ||
              `Player ${index + 1}`,
            seatNumber: player?.seatNumber || index + 1,
            round: player?.roundScore ?? null,
            total: player?.totalScore ?? player?.score ?? '-',
            cards: player?.cards || [],
          }))
      : Array.from({ length: game?.maxPlayers || 4 }, (_, i) => ({
          id: `${i + 1}`,
          userId: null,
          name: i === 0 ? 'You' : `Player ${i + 1}`,
          seatNumber: i + 1,
          round: null,
          total: '-',
          cards: [],
        }));

  const normalizeId = (value) =>
    value === null || value === undefined ? '' : String(value);

  const normalizedUsername = (user?.username || '').trim().toLowerCase();

  const currentUserPlayer =
    playerScores.find((player) => {
      if (
        normalizeId(player.userId) &&
        normalizeId(user?.userId) &&
        normalizeId(player.userId) === normalizeId(user.userId)
      ) {
        return true;
      }

      const playerName = (player.name || '').trim().toLowerCase();
      return Boolean(normalizedUsername) && playerName === normalizedUsername;
    }) || null;

  const isSamePlayer = (a, b) => {
    if (!a || !b) return false;

    const aId = normalizeId(a.userId);
    const bId = normalizeId(b.userId);

    if (aId && bId) return aId === bId;

    const aName = (a.name || '').trim().toLowerCase();
    const bName = (b.name || '').trim().toLowerCase();

    return Boolean(aName) && aName === bName;
  };

  const otherPlayers = currentUserPlayer
    ? playerScores.filter((player) => !isSamePlayer(player, currentUserPlayer))
    : [...playerScores];

  const playersByPosition = { top: null, left: null, right: null, bottom: null };

  if (currentUserPlayer) {
    playersByPosition.bottom = currentUserPlayer;
    ['top', 'left', 'right'].forEach((position, index) => {
      playersByPosition[position] = otherPlayers[index] || null;
    });
  } else {
    ['top', 'left', 'right', 'bottom'].forEach((position, index) => {
      playersByPosition[position] = otherPlayers[index] || null;
    });
  }

  const isHost = Boolean(
    game?.players?.some(
      (player) => player?.userId === user?.userId && player?.seatNumber === 1
    )
  );

  const showHostLobbyModal = isHost && effectiveStatus === 'WAITING';

  const waitingPlayers = [...(game?.players || [])].sort(
    (a, b) => (a?.seatNumber || 0) - (b?.seatNumber || 0)
  );

  const canStartGame = waitingPlayers.length >= 1;

  const discardImage = getCardImage(game?.round?.discardTop);

  return (
    <div className="game-container">
      {errorMsg && <div className="lobby-error">{errorMsg}</div>}

      {effectiveStatus !== 'WAITING' && (
        <div className="table-code-chip">
          <span>Code</span>
          <strong>{game?.gameCode || '------'}</strong>
          <button
            type="button"
            className={`table-code-copy ${copyNotice ? 'copied' : ''}`}
            onClick={onCopyCode}
            disabled={!game?.gameCode}
          >
            {copyNotice ? 'Copied!' : 'Copy'}
          </button>
        </div>
      )}

      <img src={gearIcon} className="ui-icon settings-gear" alt="settings" />
      <div className="jump-end-btn">Jump to end</div>
      <img src={profileIcon} className="ui-icon top-profile" alt="my profile" />

      <div className="table-layout">
        <PlayerHand position="top" playerMeta={playersByPosition.top} />
        <PlayerHand position="left" playerMeta={playersByPosition.left} />

        <div className="player-area center">
          <div className="deck-stack">
            <div className="card-slot dealt-card center-pile-card">
              <img src={cardBack} alt="Draw pile" className="playing-card-img" />
              {game?.round?.drawPileCount !== undefined && (
                <div className="pile-count-badge">{game.round.drawPileCount}</div>
              )}
            </div>

            <div className="card-slot dealt-card center-pile-card discard-pile">
              {discardImage ? (
                <img
                  src={discardImage}
                  alt={getCardAlt(game?.round?.discardTop)}
                  className="playing-card-img"
                />
              ) : (
                <span className="discard-placeholder">Discard</span>
              )}
            </div>
          </div>
        </div>

        <PlayerHand position="right" playerMeta={playersByPosition.right} />
        <PlayerHand position="bottom" playerMeta={playersByPosition.bottom} />
      </div>

      <div className="help-button">?</div>

      {!showHostLobbyModal && (
        <div className={`score-ledger ${isLedgerOpen ? 'open' : ''}`}>
          <button
            type="button"
            className="ledger-toggle"
            onClick={() => setIsLedgerOpen((open) => !open)}
          >
            {isLedgerOpen ? 'Hide Ledger' : 'Show Ledger'}
          </button>

          <div className="scoreboard-container">
            <div className="scoreboard-header">
              <h2 className="scoreboard-title">Score Ledger</h2>
              <span className="scoreboard-status">
                Round {game?.currentRound || 0}
              </span>
            </div>

            <div className="scoreboard-table">
              <div className="scoreboard-row scoreboard-row-head">
                <span>Player</span>
                <span>Round</span>
                <span>Total</span>
              </div>

              {playerScores.map((player) => (
                <div key={player.id} className="scoreboard-row">
                  <span>{player.name}</span>
                  <span>{player.round ?? 'Pending'}</span>
                  <span>{player.total}</span>
                </div>
              ))}
            </div>

            <p className="scoreboard-footnote">
              6-card golf: keep totals low. Round points post after full reveal.
            </p>
          </div>
        </div>
      )}

      {showHostLobbyModal && (
        <div className="host-lobby-overlay">
          <div className="host-lobby-card">
            <h2 className="host-lobby-title">Game Lobby</h2>

            <div className="host-lobby-code-row">
              <span className="host-lobby-code-label">Join code</span>
              <span className="host-lobby-code-value">{game?.gameCode || '------'}</span>
              <button
                type="button"
                className={`host-copy-btn ${copyNotice ? 'copied' : ''}`}
                onClick={onCopyCode}
                disabled={!game?.gameCode}
              >
                {copyNotice ? 'Copied!' : 'Copy'}
              </button>
            </div>

            <div className="host-lobby-players">
              <div className="host-lobby-players-title">Players in game</div>
              {waitingPlayers.map((player) => (
                <div
                  key={player.userId ?? player.gamePlayerId ?? `${player.username}-${player.seatNumber}`}
                  className="host-lobby-player-row"
                >
                  <span>{player.username || player.name}</span>
                  <span>Seat {player.seatNumber}</span>
                </div>
              ))}
            </div>

            {startError && <div className="host-lobby-error">{startError}</div>}

            <button
              type="button"
              className="host-start-btn"
              onClick={onStartGame}
              disabled={!canStartGame || startingGame}
            >
              {startingGame ? 'Starting...' : 'Start Game'}
            </button>
          </div>
        </div>
      )}

      {copyNotice && <div className="copy-toast">{copyNotice}</div>}
    </div>
  );
}