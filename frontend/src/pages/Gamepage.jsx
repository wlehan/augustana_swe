import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import './Gamepage.css';
import gearIcon from '../assets/Icon.png';
import profileIcon from '../assets/profile.png';
import AudioSettingsButton from '../components/AudioSettingsButton';
import { useAudio } from '../audio/AudioContext';
import {
  discardCard,
  drawCard,
  flipInitialCard,
  getGame,
  getGameState,
  leaveGame,
  startGame,
  swapCard,
} from '../services/gameApi';
import {
  clearStoredSession,
  hasAuthenticatedSession,
  isUnauthorizedError,
  readStoredSession,
} from '../services/session';
import {
  readStoredUserProfile,
  readStoredUserProfiles,
  recordCompletedGameStats,
} from '../services/profile';
import ProfileModal from '../components/ProfileModal';
import { normalizeId, getOpponentPositionOrder } from './gameUtils';
import GameTable from './GameTable';
import HeldCardBar from './HeldCardBar';
import TurnBanner from './TurnBanner';
import ScoreLedger from './ScoreLedger';
import HelpModal from './HelpModal';
import LobbyModal from './LobbyModal';
import RoundSummaryModal from './RoundSummaryModal';
import GameOverModal from './GameOverModal';

/**
 * Main multiplayer game screen. It polls lobby/game state, maps players into
 * viewer-relative table positions, and delegates rendering to focused table,
 * modal, and control components.
 */
export default function GamePage() {
  const navigate = useNavigate();
  const { playSound } = useAudio();
  const [params] = useSearchParams();
  const gameId = params.get('gameId');

  const [game, setGame] = useState(null);
  const [errorMsg, setErrorMsg] = useState('');
  const [isLedgerOpen, setIsLedgerOpen] = useState(false);
  const [isProfileOpen, setIsProfileOpen] = useState(false);
  const [isHelpOpen, setIsHelpOpen] = useState(false);
  const [startingGame, setStartingGame] = useState(false);
  const [leavingGame, setLeavingGame] = useState(false);
  const [startError, setStartError] = useState('');
  const [copyNotice, setCopyNotice] = useState('');

  const [actionBusy, setActionBusy] = useState(false);
  const [actionError, setActionError] = useState('');
  const [pendingDiscard, setPendingDiscard] = useState(false);

  const prevRoundRef = useRef(null);
  const completedStatsRecordedRef = useRef(false);
  const [showRoundSummary, setShowRoundSummary] = useState(false);
  const [roundSummaryData, setRoundSummaryData] = useState(null);

  const user = useMemo(() => {
    return readStoredSession();
  }, []);
  const [userProfile, setUserProfile] = useState(() => readStoredUserProfile(user));
  const [storedProfiles, setStoredProfiles] = useState(() => readStoredUserProfiles());

  const handleProfileChange = useCallback((updatedProfile) => {
    setUserProfile(updatedProfile);
    setStoredProfiles(readStoredUserProfiles());
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

  useEffect(() => {
    if (!isProfileOpen) {
      return undefined;
    }

    const handleEscape = (event) => {
      if (event.key === 'Escape') {
        setIsProfileOpen(false);
      }
    };

    document.addEventListener('keydown', handleEscape);
    return () => {
      document.removeEventListener('keydown', handleEscape);
    };
  }, [isProfileOpen]);

  const findProfileImageForPlayer = useCallback((player) => {
    if (player?.profileImage) {
      return player.profileImage;
    }

    const playerUserId = normalizeId(player?.userId);
    const sessionUserId = normalizeId(user?.userId);
    const playerUsername = (player?.username || player?.displayName || player?.name || '').trim().toLowerCase();
    const sessionUsername = (user?.username || '').trim().toLowerCase();

    if (
      (playerUserId && sessionUserId && playerUserId === sessionUserId) ||
      (playerUsername && sessionUsername && playerUsername === sessionUsername)
    ) {
      return userProfile.profileImage || null;
    }

    return storedProfiles.find((profile) => {
      const profileUserId = normalizeId(profile?.userId);
      const profileUsername = (profile?.username || '').trim().toLowerCase();
      return (
        (playerUserId && profileUserId && playerUserId === profileUserId) ||
        (playerUsername && profileUsername && playerUsername === profileUsername)
      );
    })?.profileImage || null;
  }, [storedProfiles, user, userProfile.profileImage]);

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
          profileImage: findProfileImageForPlayer(player),
        }));
    }
    return Array.from({ length: game?.maxPlayers || 4 }, (_, i) => ({
      id: `${i + 1}`, userId: null, gamePlayerId: null,
      name: i === 0 ? 'You' : `Player ${i + 1}`,
      seatNumber: i + 1, roundScore: null, total: '-', cards: [], heldCard: null, initialFlipsCount: 0,
      profileImage: i === 0 ? userProfile.profileImage : null,
    }));
  }, [findProfileImageForPlayer, game, userProfile.profileImage]);

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

  const playersByPosition = { top: null, left: null, right: null, bottom: null };
  const currentPlayerIndex = currentUserPlayer
    ? playerScores.findIndex((p) => isSamePlayer(p, currentUserPlayer))
    : -1;

  if (currentPlayerIndex >= 0) {
    const rotatedOpponents = [
      ...playerScores.slice(currentPlayerIndex + 1),
      ...playerScores.slice(0, currentPlayerIndex),
    ];
    const opponentPositionOrder = getOpponentPositionOrder(rotatedOpponents.length);

    playersByPosition.bottom = playerScores[currentPlayerIndex];
    opponentPositionOrder.forEach((pos, i) => {
      playersByPosition[pos] = rotatedOpponents[i] || null;
    });
  } else if (playerScores.length > 0) {
    const [fallbackPlayer, ...fallbackOpponents] = playerScores;
    const opponentPositionOrder = getOpponentPositionOrder(fallbackOpponents.length);

    playersByPosition.bottom = fallbackPlayer;
    opponentPositionOrder.forEach((pos, i) => {
      playersByPosition[pos] = fallbackOpponents[i] || null;
    });
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

  useEffect(() => {
    if (!isGameComplete || completedStatsRecordedRef.current) {
      return;
    }

    const updatedProfile = recordCompletedGameStats({
      session: user,
      gameId,
      players: playerScores,
    });

    completedStatsRecordedRef.current = true;
    handleProfileChange(updatedProfile);
  }, [gameId, handleProfileChange, isGameComplete, playerScores, user]);

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
  }, [actionBusy, redirectToLogin]);

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

  const handleLeaveGame = async () => {
    if (!gameId || leavingGame) return;

    setLeavingGame(true);
    setActionError('');
    setStartError('');

    try {
      await leaveGame({ gameId });
      localStorage.removeItem('active_game');
      navigate('/game-selection');
    } catch (e) {
      if (isUnauthorizedError(e)) {
        redirectToLogin();
        return;
      }
      setActionError(e?.response?.data?.message || e?.response?.data?.error || 'Could not leave game.');
    } finally {
      setLeavingGame(false);
    }
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

  const canDiscardHeldCard = isMyTurn && myHeldCard && currentDrawSource === 'STOCK';

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
      >
        <button
          className="settings-item danger"
          type="button"
          onClick={handleLeaveGame}
          disabled={leavingGame}
        >
          <span>{leavingGame ? 'Leaving...' : 'Leave game'}</span>
        </button>
      </AudioSettingsButton>
      <button
        type="button"
        className="top-profile top-profile-btn"
        onClick={() => setIsProfileOpen(true)}
        aria-label="Open profile"
      >
        <img
          src={userProfile.profileImage || profileIcon}
          className="top-profile-img profile-image"
          alt="my profile"
        />
      </button>

      <TurnBanner
        isActivePlaying={isActivePlaying}
        isMyTurn={isMyTurn}
        roundStatus={roundStatus}
        finalTriggerPlayer={finalTriggerPlayer}
        currentTurnPlayer={currentTurnPlayer}
        myHeldCard={myHeldCard}
        pendingDiscard={pendingDiscard}
        isSetupPhase={isSetupPhase}
        game={game}
        myInitialFlips={myInitialFlips}
      />

      <GameTable
        playersByPosition={playersByPosition}
        isMyTurn={isMyTurn}
        myHeldCard={myHeldCard}
        game={game}
        handleDrawFromStock={handleDrawFromStock}
        handleDrawFromDiscard={handleDrawFromDiscard}
        handleFlipInitial={handleFlipInitial}
        handleSwap={handleSwap}
        handleSelectFlip={handleSelectFlip}
        isSetupPhase={isSetupPhase}
        myInitialFlips={myInitialFlips}
        pendingDiscard={pendingDiscard}
        myCardHighlight={myCardHighlight}
      />

      <HeldCardBar
        myHeldCard={myHeldCard}
        pendingDiscard={pendingDiscard}
        canDiscardHeldCard={canDiscardHeldCard}
        actionBusy={actionBusy}
        handleDiscardClick={handleDiscardClick}
        onCancelDiscard={() => setPendingDiscard(false)}
      />

      {actionError && <div className="lobby-error action-error">{actionError}</div>}

      <button
        type="button"
        className="help-button"
        onClick={() => setIsHelpOpen(true)}
      >
        ?
      </button>

      <ScoreLedger
        isLedgerOpen={isLedgerOpen}
        setIsLedgerOpen={setIsLedgerOpen}
        game={game}
        playerScores={playerScores}
      />

      <LobbyModal
        showHostLobbyModal={showHostLobbyModal}
        game={game}
        waitingPlayers={waitingPlayers}
        findProfileImageForPlayer={findProfileImageForPlayer}
        startError={startError}
        canStartGame={canStartGame}
        startingGame={startingGame}
        onCopyCode={onCopyCode}
        copyNotice={copyNotice}
        onStartGame={onStartGame}
      />

      <RoundSummaryModal
        showRoundSummary={showRoundSummary}
        roundSummaryData={roundSummaryData}
        onClose={() => setShowRoundSummary(false)}
      />

      <GameOverModal
        isGameComplete={isGameComplete}
        playerScores={playerScores}
      />

      {copyNotice && <div className="copy-toast">{copyNotice}</div>}

      <HelpModal
        isOpen={isHelpOpen}
        onClose={() => setIsHelpOpen(false)}
      />

      {isProfileOpen && (
        <ProfileModal
          user={user}
          userProfile={userProfile}
          fallbackImage={profileIcon}
          onClose={() => setIsProfileOpen(false)}
          onProfileChange={handleProfileChange}
        />
      )}
    </div>
  );
}
