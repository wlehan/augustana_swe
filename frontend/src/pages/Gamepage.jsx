import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import './Gamepage.css';
import gearIcon from '../assets/Icon.png';
import profileIcon from '../assets/profile.png';
import cardBack from '../assets/cards/card_back.png';
import AudioSettingsButton from '../components/AudioSettingsButton';
import { useAudio } from '../audio/AudioContext';
import {
  discardCard,
  drawCard,
  flipInitialCard,
  getGame,
  getGameState,
  startGame,
  swapCard,
} from '../services/gameApi';
import {
  clearStoredSession,
  hasAuthenticatedSession,
  isUnauthorizedError,
  readStoredSession,
} from '../services/session';

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
  CLUBS:    { ACE: aceclubs, TWO: twoclubs, THREE: threeclubs, FOUR: fourclubs, FIVE: fiveclubs, SIX: sixclubs, SEVEN: sevenclubs, EIGHT: eightclubs, NINE: nineclubs, TEN: tenclubs, JACK: jackclubs, QUEEN: queenclubs, KING: kingclubs },
  DIAMONDS: { ACE: acediamonds, TWO: twodiamonds, THREE: threediamonds, FOUR: fourdiamonds, FIVE: fivediamonds, SIX: sixdiamonds, SEVEN: sevendiamonds, EIGHT: eightdiamonds, NINE: ninediamonds, TEN: tendiamonds, JACK: jackdiamonds, QUEEN: queendiamonds, KING: kingdiamonds },
  HEARTS:   { ACE: acehearts, TWO: twohearts, THREE: threehearts, FOUR: fourhearts, FIVE: fivehearts, SIX: sixhearts, SEVEN: sevenhearts, EIGHT: eighthearts, NINE: ninehearts, TEN: tenhearts, JACK: jackhearts, QUEEN: queenhearts, KING: kinghearts },
  SPADES:   { ACE: acespades, TWO: twospades, THREE: threespades, FOUR: fourspades, FIVE: fivespades, SIX: sixspades, SEVEN: sevenspades, EIGHT: eightspades, NINE: ninespades, TEN: tenspades, JACK: jackspades, QUEEN: queenspades, KING: kingspades },
};

function getCardImage(card) {
  if (!card) return null;
  if (!card.faceUp && !card.revealedToViewer) return cardBack;
  return CARD_IMAGES?.[card.suit]?.[card.rank] || null;
}

function formatCardPart(value) {
  if (!value) return '';
  return value.charAt(0).toUpperCase() + value.slice(1).toLowerCase();
}

function getCardName(card) {
  if (!card) return 'Card';
  if (!card.faceUp && !card.revealedToViewer) return 'Face-down card';
  return `${formatCardPart(card.rank)} of ${formatCardPart(card.suit)}`;
}

function getCardAlt(card) {
  if (!card) return 'Card slot';
  return getCardName(card);
}


const PLAYER_HAND_SLOTS = [1, 2, 3, 4, 5, 6];

function PlayerHand({ position, playerMeta, onCardClick, cardHighlight }) {
  const cardsByPosition = useMemo(
    () => new Map((playerMeta?.cards || []).map((c) => [c.position, c])),
    [playerMeta?.cards]
  );
  const isClickable = Boolean(onCardClick);

  const prevFaceUpRef = useRef({});
  const [flippingSlots, setFlippingSlots] = useState(new Set());

  useEffect(() => {
    const prev = prevFaceUpRef.current;
    const newlyFlipped = [];
    PLAYER_HAND_SLOTS.forEach((slot) => {
      const card = cardsByPosition.get(slot);
      if (card && card.faceUp && !prev[slot]) {
        newlyFlipped.push(slot);
      }
      prev[slot] = card?.faceUp ?? false;
    });
    if (newlyFlipped.length > 0) {
      const start = setTimeout(() => {
        setFlippingSlots(new Set(newlyFlipped));
      }, 0);
      const end = setTimeout(() => {
        setFlippingSlots(new Set());
      }, 320);
      return () => {
        clearTimeout(start);
        clearTimeout(end);
      };
    }
  }, [cardsByPosition]);

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
        {PLAYER_HAND_SLOTS.map((slot, index) => {
          const card = cardsByPosition.get(slot);
          const imageSrc = getCardImage(card);
          const hl = cardHighlight?.(slot, card);
          const isFlipping = flippingSlots.has(slot);

          return (
            <div
              key={slot}
              className={`card-slot ${card ? 'dealt-card' : ''} ${isClickable ? 'card-clickable' : ''} ${hl ? `card-hl-${hl}` : ''}`}
              style={card ? { animationDelay: `${index * 90}ms` } : undefined}
              onClick={onCardClick ? () => onCardClick(slot, card) : undefined}
            >
              {imageSrc && (
                <img
                  src={imageSrc}
                  alt={getCardAlt(card)}
                  className={`playing-card-img ${isFlipping ? 'card-flip-anim' : ''}`}
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
  const navigate = useNavigate();
  const { playSound } = useAudio();
  const [params] = useSearchParams();
  const gameId = params.get('gameId');

  const [game, setGame] = useState(null);
  const [errorMsg, setErrorMsg] = useState('');
  const [isLedgerOpen, setIsLedgerOpen] = useState(false);
  const [isHelpOpen, setIsHelpOpen] = useState(false);
  const [startingGame, setStartingGame] = useState(false);
  const [startError, setStartError] = useState('');
  const [copyNotice, setCopyNotice] = useState('');

  const [actionBusy, setActionBusy] = useState(false);
  const [actionError, setActionError] = useState('');
  const [pendingDiscard, setPendingDiscard] = useState(false);

  const prevRoundRef = useRef(null);
  const [showRoundSummary, setShowRoundSummary] = useState(false);
  const [roundSummaryData, setRoundSummaryData] = useState(null);

  const user = useMemo(() => {
    return readStoredSession();
  }, []);

  const redirectToLogin = useCallback(() => {
    clearStoredSession();
    navigate('/login', { replace: true });
  }, [navigate]);

  const effectiveStatus = game?.status ?? game?.gameStatus ?? null;

  useEffect(() => {
    if (!hasAuthenticatedSession(user)) {
      redirectToLogin();
    }
  }, [redirectToLogin, user]);

  useEffect(() => {
    if (!hasAuthenticatedSession(user)) {
      return undefined;
    }

    if (!gameId) { setErrorMsg('Missing gameId in URL.'); return; }

    let cancelled = false;

    const loadGame = async () => {
      try {
        if (effectiveStatus && effectiveStatus !== 'WAITING') {
          const data = await getGameState({ gameId, userId: user?.userId });
          if (!cancelled) {
            const newRound = data.currentRound;
            if (prevRoundRef.current !== null && newRound !== prevRoundRef.current) {
              const prevScores = data.allRoundScores?.find(
                (r) => r.roundNumber === prevRoundRef.current
              );
              if (prevScores) {
                setRoundSummaryData({ roundNumber: prevRoundRef.current, scores: prevScores.perPlayerScores, players: data.players });
                setShowRoundSummary(true);
              }
            }
            prevRoundRef.current = newRound;
            setGame(data);
            setErrorMsg('');
          }
          return;
        }

        const lobbyData = await getGame({ gameId });
        if (cancelled) return;

        if (lobbyData?.status && lobbyData.status !== 'WAITING') {
          const stateData = await getGameState({ gameId, userId: user?.userId });
          if (!cancelled) {
            prevRoundRef.current = stateData.currentRound;
            setGame(stateData);
            setErrorMsg('');
          }
          return;
        }

        setGame(lobbyData);
        setErrorMsg('');
      } catch (e) {
        if (!cancelled && isUnauthorizedError(e)) {
          redirectToLogin();
          return;
        }
        if (!cancelled) {
          setErrorMsg(
            e?.response?.data?.message || e?.response?.data?.error || 'Could not load game.'
          );
        }
      }
    };

    loadGame();
    const intervalId = setInterval(loadGame, 1500);
    return () => { cancelled = true; clearInterval(intervalId); };
  }, [effectiveStatus, gameId, redirectToLogin, user]);

  const playerScores = useMemo(() => {
    if (Array.isArray(game?.players) && game.players.length > 0) {
      return [...game.players]
        .sort((a, b) => (a?.seatNumber || 0) - (b?.seatNumber || 0))
        .map((player, index) => ({
          id: player?.gamePlayerId || player?.userId || `${index}`,
          userId: player?.userId || null,
          gamePlayerId: player?.gamePlayerId || null,
          name: player?.displayName || player?.username || player?.name || `Player ${index + 1}`,
          seatNumber: player?.seatNumber || index + 1,
          roundScore: player?.roundScore ?? null,
          total: player?.totalScore ?? player?.score ?? '-',
          cards: player?.cards || [],
          heldCard: player?.heldCard || null,
          initialFlipsCount: player?.initialFlipsCount ?? 0,
        }));
    }
    return Array.from({ length: game?.maxPlayers || 4 }, (_, i) => ({
      id: `${i + 1}`, userId: null, gamePlayerId: null,
      name: i === 0 ? 'You' : `Player ${i + 1}`,
      seatNumber: i + 1, roundScore: null, total: '-', cards: [], heldCard: null, initialFlipsCount: 0,
    }));
  }, [game]);

  const normalizeId = (v) => (v === null || v === undefined ? '' : String(v));

  const currentUserPlayer = useMemo(() => {
    const normalizedUsername = (user?.username || '').trim().toLowerCase();
    return playerScores.find((p) => {
      if (normalizeId(p.userId) && normalizeId(user?.userId) && normalizeId(p.userId) === normalizeId(user?.userId)) return true;
      return Boolean(normalizedUsername) && (p.name || '').trim().toLowerCase() === normalizedUsername;
    }) || null;
  }, [playerScores, user]);

  const isSamePlayer = (a, b) => {
    if (!a || !b) return false;
    const aId = normalizeId(a.userId), bId = normalizeId(b.userId);
    if (aId && bId) return aId === bId;
    return Boolean(a.name) && (a.name || '').trim().toLowerCase() === (b.name || '').trim().toLowerCase();
  };

  const otherPlayers = currentUserPlayer
    ? playerScores.filter((p) => !isSamePlayer(p, currentUserPlayer))
    : [...playerScores];

  const playersByPosition = { top: null, left: null, right: null, bottom: null };
  if (currentUserPlayer) {
    playersByPosition.bottom = currentUserPlayer;
    ['top', 'left', 'right'].forEach((pos, i) => { playersByPosition[pos] = otherPlayers[i] || null; });
  } else {
    ['top', 'left', 'right', 'bottom'].forEach((pos, i) => { playersByPosition[pos] = otherPlayers[i] || null; });
  }

  const isHost = Boolean(
    game?.players?.some((p) => normalizeId(p?.userId) === normalizeId(user?.userId) && p?.seatNumber === 1)
  );

  const waitingPlayers = [...(game?.players || [])].sort((a, b) => (a?.seatNumber || 0) - (b?.seatNumber || 0));
  const canStartGame = waitingPlayers.length >= 2;
  const showHostLobbyModal = isHost && effectiveStatus === 'WAITING';

  const roundStatus = game?.round?.status;
  const isSetupPhase = roundStatus === 'SETUP';
  const isActivePlaying = roundStatus === 'ACTIVE' || roundStatus === 'FINAL_TURNS';
  const isGameComplete = effectiveStatus === 'COMPLETED';

  const isMyTurn = isActivePlaying && Boolean(
    game?.round?.currentTurnUserId && String(game.round.currentTurnUserId) === String(user?.userId)
  );
  const previousIsMyTurnRef = useRef(false);
  const myHeldCard = currentUserPlayer?.heldCard || null;
  const myInitialFlips = currentUserPlayer?.initialFlipsCount ?? 0;
  const currentDrawSource = game?.round?.currentDrawSource;

  const currentTurnPlayer = useMemo(() => {
    if (!isActivePlaying) return null;
    return playerScores.find(
      (p) => String(p.gamePlayerId) === String(game?.round?.currentTurnGamePlayerId)
    ) || null;
  }, [playerScores, game?.round?.currentTurnGamePlayerId, isActivePlaying]);

  const finalTriggerPlayer = useMemo(() => {
    if (roundStatus !== 'FINAL_TURNS') return null;
    return playerScores.find(
      (p) => String(p.gamePlayerId) === String(game?.round?.finalTurnTriggeredByGamePlayerId)
    ) || null;
  }, [playerScores, game?.round?.finalTurnTriggeredByGamePlayerId, roundStatus]);

  const doAction = useCallback(async (fn) => {
    if (actionBusy) return;
    setActionBusy(true);
    setActionError('');
    try {
      const data = await fn();
      setGame(data);
      setPendingDiscard(false);
    } catch (e) {
      if (isUnauthorizedError(e)) {
        redirectToLogin();
        return;
      }
      setActionError(
        e?.response?.data?.message || e?.response?.data?.error || 'Action failed.'
      );
    } finally {
      setActionBusy(false);
    }
  }, [actionBusy]);

  const onCopyCode = async () => {
    if (!game?.gameCode) return;
    try { await navigator.clipboard.writeText(game.gameCode); } catch { window.prompt('Copy:', game.gameCode); }
    setCopyNotice('Copied!');
    setTimeout(() => setCopyNotice(''), 1600);
  };

  const onStartGame = async () => {
    if (!gameId || startingGame) return;
    if (!hasAuthenticatedSession(user)) {
      redirectToLogin();
      return;
    }
    setStartError(''); setStartingGame(true);
    try {
      const data = await startGame({ gameId, userId: user?.userId });
      setGame(data);
    } catch (e) {
      if (isUnauthorizedError(e)) {
        redirectToLogin();
        return;
      }
      setStartError(e?.response?.data?.message || e?.response?.data?.error || `Could not start game.`);
    } finally { setStartingGame(false); }
  };


  const handleFlipInitial = (position) => {
    if (myInitialFlips >= 2) return;
    doAction(() => flipInitialCard({ gameId, userId: user?.userId, position }));
  };

  const handleDrawFromStock = () => {
    if (!isMyTurn || myHeldCard) return;
    doAction(() => drawCard({ gameId, userId: user?.userId, source: 'STOCK' }));
  };

  const handleDrawFromDiscard = () => {
    if (!isMyTurn || myHeldCard) return;
    doAction(() => drawCard({ gameId, userId: user?.userId, source: 'DISCARD' }));
  };

  const handleSwap = (position) => {
    if (!isMyTurn || !myHeldCard) return;
    doAction(() => swapCard({ gameId, userId: user?.userId, position }));
  };

  const handleDiscardClick = () => {
    if (!isMyTurn || !myHeldCard || currentDrawSource !== 'STOCK') return;
    const hasFaceDown = currentUserPlayer?.cards?.some((c) => !c.faceUp);
    if (!hasFaceDown) {
      doAction(() => discardCard({ gameId, userId: user?.userId, flipPosition: null }));
    } else {
      setPendingDiscard(true);
      setActionError('');
    }
  };

  const handleSelectFlip = (position, card) => {
    if (!pendingDiscard) return;
    if (card?.faceUp) {
      setActionError('Pick a face-down card to flip.');
      return;
    }
    doAction(() => discardCard({ gameId, userId: user?.userId, flipPosition: position }));
  };

  const myCardHighlight = (slot, card) => {
    if (isSetupPhase && myInitialFlips < 2 && card && !card.faceUp) return 'setup';
    if (isMyTurn && myHeldCard && !pendingDiscard) return 'swap';
    if (isMyTurn && myHeldCard && pendingDiscard && card && !card.faceUp) return 'flip';
    return null;
  };

  const discardImage = getCardImage(game?.round?.discardTop);
  const heldCardImage = getCardImage(myHeldCard);
  const heldCardName = getCardName(myHeldCard);
  const canDiscardHeldCard = isMyTurn && myHeldCard && currentDrawSource === 'STOCK';

  const allRoundScores = game?.allRoundScores || [];
  const ledgerRounds = allRoundScores.map((rs) => {
    const perPlayer = {};
    (rs.perPlayerScores || []).forEach((s) => { perPlayer[s.gamePlayerId] = s.score; });
    return { roundNumber: rs.roundNumber, perPlayer };
  });

  useEffect(() => {
    if (isMyTurn && !previousIsMyTurnRef.current) {
      playSound('turn-chime');
    }

    previousIsMyTurnRef.current = isMyTurn;
  }, [isMyTurn, playSound]);

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

      <AudioSettingsButton
        iconSrc={gearIcon}
        iconAlt="Settings"
        className="settings-gear"
      />
      <img src={profileIcon} className="ui-icon top-profile" alt="my profile" />
      {isActivePlaying && (
        <div className={`turn-banner ${isMyTurn ? 'my-turn' : ''}`}>
          {roundStatus === 'FINAL_TURNS' && (
            <span className="final-turns-tag">Final Turns</span>
          )}
          {isMyTurn ? (
            myHeldCard
              ? pendingDiscard
                ? '🃏 Click a face-down card in your grid to flip'
                : '🃏 Swap a card — or discard below'
              : '📥 Your turn — draw from the stock or discard pile'
          ) : (
            roundStatus === 'FINAL_TURNS' && finalTriggerPlayer
              ? `Final turns after ${finalTriggerPlayer.name}. Waiting for ${currentTurnPlayer?.name || 'player'}…`
              : `Waiting for ${currentTurnPlayer?.name || 'player'}…`
          )}
        </div>
      )}

      {isSetupPhase && (
        <div className="turn-banner setup-banner">
          Round {game?.currentRound} — Flip 2 cards to start &nbsp;
          {isActivePlaying ? null : (
            <span className="setup-progress">{myInitialFlips}/2 flipped</span>
          )}
        </div>
      )}

      <div className="table-layout">
        <PlayerHand position="top"    playerMeta={playersByPosition.top} />
        <PlayerHand position="left"   playerMeta={playersByPosition.left} />
        <div className="player-area center">
          <div className="deck-stack">
            <div
              className={`card-slot dealt-card center-pile-card ${isMyTurn && !myHeldCard ? 'pile-active' : ''}`}
              onClick={isMyTurn && !myHeldCard ? handleDrawFromStock : undefined}
              title={isMyTurn && !myHeldCard ? 'Draw from stock' : ''}
            >
              <img src={cardBack} alt="Draw pile" className="playing-card-img" />
              {game?.round?.drawPileCount !== undefined && (
                <div className="pile-count-badge">{game.round.drawPileCount}</div>
              )}
            </div>
            <div
              className={`card-slot dealt-card center-pile-card discard-pile ${isMyTurn && !myHeldCard ? 'pile-active' : ''}`}
              onClick={isMyTurn && !myHeldCard ? handleDrawFromDiscard : undefined}
              title={isMyTurn && !myHeldCard ? 'Draw from discard' : ''}
            >
              {discardImage ? (
                <img src={discardImage} alt={getCardAlt(game?.round?.discardTop)} className="playing-card-img" />
              ) : (
                <span className="discard-placeholder">Discard</span>
              )}
            </div>
          </div>
        </div>

        <PlayerHand position="right"  playerMeta={playersByPosition.right} />
        <PlayerHand
          position="bottom"
          playerMeta={playersByPosition.bottom}
          onCardClick={
            (isSetupPhase && myInitialFlips < 2)
              ? (slot) => handleFlipInitial(slot)
              : (isMyTurn && myHeldCard && !pendingDiscard)
              ? (slot) => handleSwap(slot)
              : (isMyTurn && myHeldCard && pendingDiscard)
              ? handleSelectFlip
              : null
          }
          cardHighlight={myCardHighlight}
        />
      </div>

      {myHeldCard && (
        <div className="held-card-bar" aria-live="polite">
          <div className="held-card-preview">
            <div className="card-slot dealt-card held-card-slot">
              {heldCardImage && (
                <img
                  src={heldCardImage}
                  alt={`Held card: ${heldCardName}`}
                  className="playing-card-img"
                />
              )}
            </div>
          </div>
          <div className="held-card-copy">
            <div className="held-card-label">Card in hand</div>
            <div className="held-card-name">{heldCardName}</div>
            <div className="held-card-instructions">
              {pendingDiscard
                ? 'Choose a face-down card to flip.'
                : canDiscardHeldCard
                ? 'Swap it with one of your cards, or discard it.'
                : 'Swap it with one of your cards.'}
            </div>
          </div>
          {canDiscardHeldCard && !pendingDiscard && (
            <button
              type="button"
              className="discard-btn"
              onClick={handleDiscardClick}
              disabled={actionBusy}
            >
              Discard
            </button>
          )}
          {pendingDiscard && (
            <button
              type="button"
              className="discard-cancel-btn"
              onClick={() => setPendingDiscard(false)}
              disabled={actionBusy}
            >
              Cancel
            </button>
          )}
        </div>
      )}

      {actionError && <div className="lobby-error action-error">{actionError}</div>}

    <button
      type="button"
      className="help-button"
      onClick={() => setIsHelpOpen(true)}
    >
      ?
    </button>

    {isHelpOpen && (
      <div className="help-overlay" onClick={() => setIsHelpOpen(false)}>
        <div
          className="help-popup"
          onClick={(e) => e.stopPropagation()}
        >
        <h2>How to Play</h2>

        <p><strong>Goal:</strong> Finish with the lowest total score.</p>

        <h3>Setup</h3>
        <ul>
          <li>Each player has 6 cards (2 rows of 3).</li>
          <li>2 cards start face-up, 4 face-down.</li>
        </ul>

        <h3>Your Turn</h3>
        <ul>
          <li>Draw a card from the deck or discard pile.</li>
          <li>Swap it with one of your 6 cards <em>or</em> discard it.</li>
          <li>If you choose to discard, you must select a face down card from your hand and flip it.</li>
        </ul>

        <h3>Ending a Round</h3>
        <ul>
          <li>When someone's hand is fully face-up, the round ends.</li>
          <li>Everyone else gets one final turn.</li>
        </ul>

        <h3>Scoring</h3>
        <ul>
          <li>Number cards = face value</li>
          <li>Ace = 1 point</li>
          <li>King = 0 points</li>
          <li>Other face cards = 10 points</li>
          <li>Pairs in a column = 0 points</li>
          <li>Two = -2 points</li>
          <li>Pair of 2s in a column = 0 points</li>
        </ul>

        <h4>Ending the Game</h4>
        <ul>
          <li>After 9 rounds, the player with the lowest total score wins.</li>
        </ul>

        <p><strong>Tip:</strong> Try to match columns and keep high cards out.</p>
          <button
            type="button"
            className="help-close-btn"
            onClick={() => setIsHelpOpen(false)}
          >
            Close
          </button>
        </div>
      </div>
    )}

      {!showHostLobbyModal && (
        <div className={`score-ledger ${isLedgerOpen ? 'open' : ''}`}>
          <button
            type="button"
            className="ledger-toggle"
            onClick={() => setIsLedgerOpen((o) => !o)}
          >
            {isLedgerOpen ? 'Hide Ledger' : 'Show Ledger'}
          </button>

          <div className="scoreboard-container">
            <div className="scoreboard-header">
              <h2 className="scoreboard-title">Score Ledger</h2>
              <span className="scoreboard-status">Round {game?.currentRound || 0} / 9</span>
            </div>

            <div className="scoreboard-table">
              <div className="scoreboard-row scoreboard-row-head">
                <span>Player</span>
                {ledgerRounds.map((r) => (
                  <span key={r.roundNumber}>R{r.roundNumber}</span>
                ))}
                <span>Total</span>
              </div>

              {playerScores.map((player) => (
                <div key={player.id} className="scoreboard-row">
                  <span>{player.name}</span>
                  {ledgerRounds.map((r) => (
                    <span key={r.roundNumber}>
                      {r.perPlayer[player.gamePlayerId] !== undefined
                        ? r.perPlayer[player.gamePlayerId]
                        : '-'}
                    </span>
                  ))}
                  <span>{player.total}</span>
                </div>
              ))}
            </div>

            <p className="scoreboard-footnote">
              Lowest total after 9 rounds wins. Pair in a column = 0 pts. King = 0, Two = −2.
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
              {startingGame ? 'Starting…' : 'Start Game'}
            </button>
          </div>
        </div>
      )}
      {showRoundSummary && roundSummaryData && (
        <div className="round-summary-overlay">
          <div className="round-summary-card">
            <h2 className="round-summary-title">Round {roundSummaryData.roundNumber} Complete!</h2>

            <div className="round-summary-scores">
              {(roundSummaryData.scores || []).map((s) => {
                const p = (roundSummaryData.players || []).find(
                  (pl) => String(pl.gamePlayerId) === String(s.gamePlayerId)
                );
                return (
                  <div key={s.gamePlayerId} className="round-summary-row">
                    <span>{p?.username || `Player ${s.gamePlayerId}`}</span>
                    <span className="round-summary-score">{s.score > 0 ? `+${s.score}` : s.score}</span>
                  </div>
                );
              })}
            </div>

            <button
              type="button"
              className="round-summary-close"
              onClick={() => setShowRoundSummary(false)}
            >
              Continue →
            </button>
          </div>
        </div>
      )}
      {isGameComplete && (
        <div className="game-over-overlay">
          <div className="game-over-card">
            <h2 className="game-over-title">Game Over!</h2>
            <p className="game-over-subtitle">Final Scores</p>

            <div className="game-over-scores">
              {[...playerScores]
                .sort((a, b) => (a.total ?? 999) - (b.total ?? 999))
                .map((p, i) => (
                  <div key={p.id} className={`game-over-row ${i === 0 ? 'winner' : ''}`}>
                    <span className="game-over-rank">{i === 0 ? '🏆' : `#${i + 1}`}</span>
                    <span className="game-over-name">{p.name}</span>
                    <span className="game-over-total">{p.total}</span>
                  </div>
                ))}
            </div>

            <button
              type="button"
              className="game-over-play-again"
              onClick={() => (window.location.href = '/game-selection')}
            >
              Play Again
            </button>
          </div>
        </div>
      )}

      {copyNotice && <div className="copy-toast">{copyNotice}</div>}
    </div>
  );
}
