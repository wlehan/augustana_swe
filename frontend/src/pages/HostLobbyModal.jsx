import React from 'react';
import './HostLobbyModal.css';

export default function HostLobbyModal({
  showHostLobbyModal,
  game,
  waitingPlayers,
  copyNotice,
  onCopyCode,
  startError,
  onStartGame,
  canStartGame,
  startingGame,
}) {
  if (!showHostLobbyModal) return null;

  return (
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

        <button type="button" className="host-start-btn" onClick={onStartGame} disabled={!canStartGame || startingGame}>
          {startingGame ? 'Starting…' : 'Start Game'}
        </button>
      </div>
    </div>
  );
}
