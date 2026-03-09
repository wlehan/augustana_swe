import React, { useEffect, useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import './Gamepage.css';
import gearIcon from '../assets/Icon.png';
import profileIcon from '../assets/profile.png';
import { getGame, startGame } from '../services/gameApi';

function PlayerHand({ position, playerMeta }) {
  const slots = [1, 2, 3, 4, 5, 6];
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
        {slots.map((slot) => (
          <div key={slot} className="card-slot"></div>
        ))}
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

  useEffect(() => {
    if (!gameId) {
      setErrorMsg('Missing gameId in URL.');
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
          setErrorMsg(e?.response?.data?.message || 'Could not load game.');
        }
      }
    };

    loadGame();

    // Optional: poll while in lobby (helps if players join)
    const interval = setInterval(loadGame, 2000);

    return () => {
      cancelled = true;
      clearInterval(interval);
    };
  }, [gameId]);

  const onCopyCode = async () => {
    if (!game?.gameCode) return;
    try {
      await navigator.clipboard.writeText(game.gameCode);
      setCopyNotice('Copied to clipboard');
    } catch {
      // fallback if clipboard blocked
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
      await startGame({ gameId });
      const data = await getGame({ gameId });
      setGame(data);
    } catch (e) {
      setStartError(e?.response?.data?.message || 'Could not start game.');
    } finally {
      setStartingGame(false);
    }
  };

  const playerScores = Array.isArray(game?.players) && game.players.length > 0
    ? [...game.players]
      .sort((a, b) => (a?.seatNumber || 0) - (b?.seatNumber || 0))
      .map((player, index) => ({
        id: player?.userId || `${index}`,
        userId: player?.userId || null,
        name: player?.displayName || player?.username || player?.name || `Player ${index + 1}`,
        seatNumber: player?.seatNumber || index + 1,
        round: player?.roundScore ?? null,
        total: player?.totalScore ?? player?.score ?? '-',
      }))
    : Array.from({ length: game?.maxPlayers || 4 }, (_, i) => ({
        id: `${i + 1}`,
        userId: null,
        name: i === 0 ? 'You' : `Player ${i + 1}`,
        seatNumber: i + 1,
        round: null,
        total: '-',
      }));

  const normalizeId = (value) => (value === null || value === undefined ? '' : String(value));
  const normalizedUsername = (user?.username || '').trim().toLowerCase();
  const currentUserPlayer = playerScores.find((player) => {
    if (normalizeId(player.userId) && normalizeId(user?.userId) && normalizeId(player.userId) === normalizeId(user.userId)) return true;
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
    game?.players?.some((player) => player?.userId === user?.userId && player?.seatNumber === 1)
  );
  const showHostLobbyModal = isHost && game?.status === 'WAITING';
  const waitingPlayers = [...(game?.players || [])].sort((a, b) => (a?.seatNumber || 0) - (b?.seatNumber || 0));
  const canStartGame = waitingPlayers.length >= 2;

  return (
    <div className="game-container">
      {errorMsg && <div className="lobby-error">{errorMsg}</div>}

      {game?.status !== 'WAITING' && (
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
            <div className="card-slot">Deck</div>
            <div className="card-slot">Discard</div>
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
              <span className="scoreboard-status">Round {game?.currentRound || 0}</span>
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
                <div key={player.userId} className="host-lobby-player-row">
                  <span>{player.username}</span>
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
