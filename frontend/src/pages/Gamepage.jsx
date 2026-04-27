import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import './Gamepage.css';
import './GameBoard.css';
import './TurnBanner.css';
import './Notifications.css';
import gearIcon from '../assets/Icon.png';
import profileIcon from '../assets/profile.png';
import {
  discardCard,
  drawCard,
  flipInitialCard,
  getGame,
  getGameState,
  startGame,
  swapCard,
} from '../services/gameApi';
import { cardBack, getCardAlt, getCardImage } from './cardHelpers';
import PlayerHand from './PlayerHand';
import HeldCardBar from './HeldCardBar';
import ScoreLedger from './ScoreLedger';
import HostLobbyModal from './HostLobbyModal';
import RoundSummaryOverlay from './RoundSummaryOverlay';
import GameOverOverlay from './GameOverOverlay';


export default function GamePage() {
  const [params] = useSearchParams();
  const gameId = params.get('gameId');

  const [game, setGame] = useState(null);
  const [errorMsg, setErrorMsg] = useState('');
  const [isLedgerOpen, setIsLedgerOpen] = useState(false);
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
    const raw = localStorage.getItem('demo_user');
    if (!raw) return null;
    try { return JSON.parse(raw); } catch { return null; }
  }, []);

  const effectiveStatus = game?.status ?? game?.gameStatus ?? null;

  useEffect(() => {
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
  }, [gameId, effectiveStatus, user?.userId]);

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
    setStartError(''); setStartingGame(true);
    try {
      const data = await startGame({ gameId, userId: user?.userId });
      setGame(data);
    } catch (e) {
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

  const allRoundScores = game?.allRoundScores || [];
  const ledgerRounds = allRoundScores.map((rs) => {
    const perPlayer = {};
    (rs.perPlayerScores || []).forEach((s) => { perPlayer[s.gamePlayerId] = s.score; });
    return { roundNumber: rs.roundNumber, perPlayer };
  });

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
      <HeldCardBar
        myHeldCard={myHeldCard}
        pendingDiscard={pendingDiscard}
        currentDrawSource={currentDrawSource}
        actionBusy={actionBusy}
        handleDiscardClick={handleDiscardClick}
        setPendingDiscard={setPendingDiscard}
      />
      {isSetupPhase && myInitialFlips < 2 && (
        <div className="setup-hint">
          Click {2 - myInitialFlips} more card{2 - myInitialFlips > 1 ? 's' : ''} in your grid to flip
        </div>
      )}
      {actionError && (
        <div className="action-error" onClick={() => setActionError('')}>{actionError}</div>
      )}
      <ScoreLedger
        showHostLobbyModal={showHostLobbyModal}
        isLedgerOpen={isLedgerOpen}
        setIsLedgerOpen={setIsLedgerOpen}
        game={game}
        playerScores={playerScores}
        ledgerRounds={ledgerRounds}
      />
      <HostLobbyModal
        showHostLobbyModal={showHostLobbyModal}
        game={game}
        waitingPlayers={waitingPlayers}
        copyNotice={copyNotice}
        onCopyCode={onCopyCode}
        startError={startError}
        onStartGame={onStartGame}
        canStartGame={canStartGame}
        startingGame={startingGame}
      />
      <RoundSummaryOverlay
        showRoundSummary={showRoundSummary}
        roundSummaryData={roundSummaryData}
        onClose={() => setShowRoundSummary(false)}
      />
      <GameOverOverlay isGameComplete={isGameComplete} playerScores={playerScores} />

      {copyNotice && <div className="copy-toast">{copyNotice}</div>}
      <div className="help-button">?</div>
    </div>
  );
}
