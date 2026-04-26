import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './Gamepage.css';
import './TutorialPage.css';
import cardBack from '../assets/cards/card_back.png';
import profileIcon from '../assets/profile.png';

import {
  discardCard,
  drawCard,
  flipInitialCard,
  swapCard,
} from '../services/gameApi';

import {
  startTutorial,
  getTutorialState,
  botFlip,
  botTurn,
} from '../services/tutorialApi';

// ── Card image imports (same as Gamepage) ────────────────────────────────────
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

// Step ordering for the progress dots
const STEP_ORDER = [
  'WELCOME', 'FLIP_FIRST', 'FLIP_SECOND', 'WAIT_FOR_OTHERS_TO_FLIP',
  'YOUR_TURN_DRAW', 'YOUR_TURN_DECIDE', 'BOT_TURN',
  'FINAL_TURNS', 'ROUND_OVER', 'TUTORIAL_COMPLETE',
];

// ── HintPanel ────────────────────────────────────────────────────────────────

function HintPanel({ step, title, description, onDismiss, onOk, okLabel, okDisabled }) {
  const stepIndex = STEP_ORDER.indexOf(step);

  return (
    <div className="tutorial-hint-panel" role="status" aria-live="polite">
      <div className="hint-header">
        <span className="hint-step-badge">Tutorial</span>
        <h3 className="hint-title">{title}</h3>
        <button
          type="button"
          className="hint-dismiss-btn"
          onClick={onDismiss}
          aria-label="Hide hint panel"
        >
          ×
        </button>
      </div>

      <p className="hint-description">{description}</p>

      <div className="hint-footer">
        <div className="hint-progress-dots" aria-hidden="true">
          {STEP_ORDER.map((s, i) => (
            <span
              key={s}
              className={`hint-dot ${i === stepIndex ? 'active' : i < stepIndex ? 'done' : ''}`}
            />
          ))}
        </div>
      </div>
    </div>
  );
}

// ── PlayerHand (same as Gamepage) ─────────────────────────────────────────────

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
      if (card && card.faceUp && !prev[slot]) newlyFlipped.push(slot);
      prev[slot] = card?.faceUp ?? false;
    });
    if (newlyFlipped.length > 0) {
      const start = setTimeout(() => setFlippingSlots(new Set(newlyFlipped)), 0);
      const end = setTimeout(() => setFlippingSlots(new Set()), 320);
      return () => { clearTimeout(start); clearTimeout(end); };
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

// ── TutorialPage ──────────────────────────────────────────────────────────────

export default function TutorialPage() {
  const navigate = useNavigate();

  const user = useMemo(() => {
    const raw = localStorage.getItem('demo_user');
    if (!raw) return null;
    try { return JSON.parse(raw); } catch { return null; }
  }, []);

  // Tutorial state from the backend
  const [tutState, setTutState] = useState(null);   // full TutorialStateResponse
  const [gameId, setGameId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState('');

  // UI state
  const [hintVisible, setHintVisible] = useState(true);
  const [actionBusy, setActionBusy] = useState(false);
  const [actionError, setActionError] = useState('');
  const [pendingDiscard, setPendingDiscard] = useState(false);
  const [botThinking, setBotThinking] = useState(false);
  const [showComplete, setShowComplete] = useState(false);
  const [showSpecialRules, setShowSpecialRules] = useState(false);

  const game = tutState?.gameState || null;
  const currentStep = tutState?.currentStep || 'WELCOME';
  const stepTitle = tutState?.stepTitle || '';
  const stepDescription = tutState?.stepDescription || '';
  const humanFlipsCompleted = tutState?.humanFlipsCompleted ?? 0;
  const allPlayersReady = tutState?.allPlayersReady ?? false;

  // ── Bootstrap: start tutorial on mount ──────────────────────────────────────
  useEffect(() => {
    if (!user?.userId) { navigate('/login'); return; }

    let cancelled = false;
    (async () => {
      try {
        const data = await startTutorial({ userId: user.userId });
        if (cancelled) return;
        setTutState(data);
        setGameId(data.gameId);
      } catch (e) {
        if (!cancelled) setErrorMsg(e?.response?.data?.message || 'Could not start tutorial.');
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();

    return () => { cancelled = true; };
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  // ── Refresh tutorial state from backend ──────────────────────────────────────
  const refreshState = useCallback(async (gId) => {
    const id = gId ?? gameId;
    if (!id || !user?.userId) return;
    try {
      const data = await getTutorialState({ gameId: id, userId: user.userId });
      setTutState(data);
      if (data.currentStep === 'TUTORIAL_COMPLETE') setShowSpecialRules(true);
    } catch (e) {
      setActionError(e?.response?.data?.message || 'Could not refresh state.');
    }
  }, [gameId, user?.userId]);

  // ── Bot turn automation ───────────────────────────────────────────────────────
  // Whenever the step transitions to BOT_TURN, automatically execute it after a
  // short delay so the player can see what's happening.
  useEffect(() => {
    if (currentStep !== 'BOT_TURN' || !gameId || !user?.userId || actionBusy) return;

    let cancelled = false;
    const timer = setTimeout(async () => {
      if (cancelled) return;
      setBotThinking(true);
      try {
        const data = await botTurn({ gameId, userId: user.userId });
        if (!cancelled) {
          setTutState(data);
          setPendingDiscard(false);
          if (data.currentStep === 'TUTORIAL_COMPLETE') setShowSpecialRules(true);
        }
      } catch (e) {
        if (!cancelled) setActionError(e?.response?.data?.message || 'Bot turn failed.');
      } finally {
        if (!cancelled) setBotThinking(false);
      }
    }, 1400); // 1.4 s pause so the player can watch

    return () => { cancelled = true; clearTimeout(timer); };
  }, [currentStep, gameId, user?.userId, actionBusy]);

  // ── Bot initial flips (triggered once human has done both flips) ──────────────
  useEffect(() => {
    if (humanFlipsCompleted < 2 || allPlayersReady || !gameId || !user?.userId) return;

    let cancelled = false;
    const timer = setTimeout(async () => {
      if (cancelled) return;
      setBotThinking(true);
      try {
        const data = await botFlip({ gameId, userId: user.userId });
        if (!cancelled) setTutState(data);
      } catch (e) {
        if (!cancelled) setActionError(e?.response?.data?.message || 'Bot flip failed.');
      } finally {
        if (!cancelled) setBotThinking(false);
      }
    }, 900);

    return () => { cancelled = true; clearTimeout(timer); };
  }, [humanFlipsCompleted, allPlayersReady, gameId, user?.userId]);

  // ── Player score view ─────────────────────────────────────────────────────────
  const playerScores = useMemo(() => {
    if (!Array.isArray(game?.players) || game.players.length === 0) return [];
    return [...game.players]
      .sort((a, b) => (a?.seatNumber || 0) - (b?.seatNumber || 0))
      .map((player, index) => ({
        id: player?.gamePlayerId || player?.userId || `${index}`,
        userId: player?.userId || null,
        gamePlayerId: player?.gamePlayerId || null,
        name: player?.username === 'tutorial_bot' ? '🤖 Bot' : (player?.displayName || player?.username || 'You'),
        seatNumber: player?.seatNumber || index + 1,
        total: player?.totalScore ?? player?.score ?? '-',
        cards: player?.cards || [],
        heldCard: player?.heldCard || null,
        initialFlipsCount: player?.initialFlipsCount ?? 0,
      }));
  }, [game]);

  const humanPlayer = useMemo(
    () => playerScores.find((p) => p.seatNumber === 1) || null,
    [playerScores]
  );
  const botPlayer = useMemo(
    () => playerScores.find((p) => p.seatNumber === 2) || null,
    [playerScores]
  );

  // ── Game phase flags (same logic as Gamepage) ─────────────────────────────────
  const roundStatus = game?.round?.status;
  const isSetupPhase = roundStatus === 'SETUP';
  const isActivePlaying = roundStatus === 'ACTIVE' || roundStatus === 'FINAL_TURNS';
  const isMyTurn = isActivePlaying && Boolean(
    game?.round?.currentTurnUserId && String(game.round.currentTurnUserId) === String(user?.userId)
  );
  const myHeldCard = humanPlayer?.heldCard || null;
  const myInitialFlips = humanPlayer?.initialFlipsCount ?? 0;
  const currentDrawSource = game?.round?.currentDrawSource;

  // ── Action wrapper ─────────────────────────────────────────────────────────────
  const doAction = useCallback(async (fn) => {
    if (actionBusy) return;
    setActionBusy(true);
    setActionError('');
    try {
      // Human actions use the standard game endpoints — just need to refresh tutorial state after
      await fn();
      await refreshState();
      setPendingDiscard(false);
    } catch (e) {
      setActionError(e?.response?.data?.message || e?.response?.data?.error || 'Action failed.');
    } finally {
      setActionBusy(false);
    }
  }, [actionBusy, refreshState]);

  // ── Card action handlers ───────────────────────────────────────────────────────
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
    const hasFaceDown = humanPlayer?.cards?.some((c) => !c.faceUp);
    if (!hasFaceDown) {
      doAction(() => discardCard({ gameId, userId: user?.userId, flipPosition: null }));
    } else {
      setPendingDiscard(true);
      setActionError('');
    }
  };

  const handleSelectFlip = (position, card) => {
    if (!pendingDiscard) return;
    if (card?.faceUp) { setActionError('Pick a face-down card to flip.'); return; }
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

  // ── Loading / error screens ────────────────────────────────────────────────────
  if (loading) {
    return (
      <div className="game-container" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <div style={{ fontFamily: 'OpenDyslexic, sans-serif', fontSize: 22, color: '#f2e8cf' }}>
          Setting up your tutorial…
        </div>
      </div>
    );
  }

  if (errorMsg) {
    return (
      <div className="game-container" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 20 }}>
        <div style={{ fontFamily: 'OpenDyslexic, sans-serif', fontSize: 18, color: '#f5d4cd' }}>{errorMsg}</div>
        <button className="green-btn" onClick={() => navigate('/game-selection')}>Back to lobby</button>
      </div>
    );
  }

  return (
    <div className="game-container">

      {/* Exit tutorial */}
      <button
        type="button"
        className="tutorial-exit-btn"
        onClick={() => navigate('/game-selection')}
      >
        ← Exit Tutorial
      </button>

      {/* Bot thinking indicator */}
      {botThinking && (
        <div className="bot-thinking-banner">🤖 Bot is thinking…</div>
      )}

      {/* Turn banner (same as Gamepage) */}
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
            `🤖 Bot's turn…`
          )}
        </div>
      )}

      {isSetupPhase && (
        <div className="turn-banner setup-banner">
          Flip 2 of your cards to begin &nbsp;
          <span className="setup-progress">{myInitialFlips}/2 flipped</span>
        </div>
      )}

      {/* Main table layout */}
      <div className="table-layout">
        <PlayerHand position="top" playerMeta={botPlayer} />
        <PlayerHand position="left" playerMeta={null} />

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

        <PlayerHand position="right" playerMeta={null} />

        <PlayerHand
          position="bottom"
          playerMeta={humanPlayer}
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

      {/* Held card bar */}
      {myHeldCard && (
        <div className="held-card-bar" aria-live="polite">
          <div className="held-card-preview">
            <div className="card-slot dealt-card held-card-slot">
              {heldCardImage && (
                <img src={heldCardImage} alt={`Held card: ${heldCardName}`} className="playing-card-img" />
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
            <button type="button" className="discard-btn" onClick={handleDiscardClick} disabled={actionBusy}>
              Discard
            </button>
          )}
          {pendingDiscard && (
            <button type="button" className="discard-cancel-btn" onClick={() => setPendingDiscard(false)} disabled={actionBusy}>
              Cancel
            </button>
          )}
        </div>
      )}

      {/* Action error */}
      {actionError && (
        <div className="lobby-error" style={{ position: 'fixed', bottom: hintVisible ? 220 : 80, left: '50%', transform: 'translateX(-50%)', zIndex: 130 }}>
          {actionError}
        </div>
      )}

      {/* ── Tutorial hint panel ── */}
      {hintVisible && stepTitle && !showComplete && !showSpecialRules && (
        <HintPanel
          step={currentStep}
          title={stepTitle}
          description={stepDescription}
        />
      )}

      {/* ── Special rules overlay ── */}
      {showSpecialRules && (
        <div className="tutorial-complete-overlay">
          <div className="tutorial-complete-card">
            <h2 className="tutorial-complete-title">Special Scoring Rules</h2>

            <div className="tutorial-special-rules">
              <ul>
                <li>Kings are worth 0 points.</li>
                <li>Twos are worth -2 points.</li>
                <li>Matching ranks in the same column cancel to 0.</li>
                <li>Two 2s in one column score 0, not -4.</li>
              </ul>
            </div>

            <button
              type="button"
              className="tutorial-complete-lobby-btn"
              onClick={() => {
                setShowSpecialRules(false);
                setShowComplete(true);
              }}
            >
              Continue →
            </button>
          </div>
        </div>
      )}

      {/* ── Tutorial complete overlay ── */}
      {showComplete &&  !showSpecialRules && (
        <div className="tutorial-complete-overlay">
          <div className="tutorial-complete-card">
            <div className="tutorial-complete-trophy">🏆</div>
            <h2 className="tutorial-complete-title">Tutorial Complete!</h2>
            <p className="tutorial-complete-subtitle">
              You've played a full round of Six-Card Golf. You're ready for the real thing — may your score stay low!
            </p>

            <div className="tutorial-complete-scores">
              {[...playerScores]
                .sort((a, b) => (a.total ?? 999) - (b.total ?? 999))
                .map((p, i) => (
                  <div key={p.id} className={`tutorial-complete-score-row ${i === 0 ? 'winner' : ''}`}>
                    <span>{i === 0 ? '🥇 ' : ''}{p.name}</span>
                    <strong>{p.total}</strong>
                  </div>
                ))}
            </div>

            <div className="tutorial-complete-actions">
              <button
                type="button"
                className="tutorial-complete-lobby-btn"
                onClick={() => navigate('/game-selection')}
              >
                Go to Lobby
              </button>
              <button
                type="button"
                className="tutorial-complete-replay-btn"
                onClick={() => window.location.reload()}
              >
                Play Again
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
