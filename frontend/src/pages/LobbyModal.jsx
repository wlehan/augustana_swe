import React from 'react';
import profileIcon from '../assets/profile.png';

/**
 * Host-only waiting-room dialog with joined players, copied game code, and the
 * start button once enough players have joined.
 */
export default function LobbyModal({
  showHostLobbyModal,
  game,
  waitingPlayers,
  findProfileImageForPlayer,
  startError,
  canStartGame,
  startingGame,
  onCopyCode,
  copyNotice,
  onStartGame
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
              key={player.gamePlayerId ?? player.userId ?? `${player.username}-${player.seatNumber}`}
              className="host-lobby-player-row"
            >
              <span className="host-lobby-player-name">
                <img
                  src={findProfileImageForPlayer(player) || profileIcon}
                  className="host-lobby-player-img"
                  alt=""
                />
                <span>{player.username || player.name}</span>
              </span>
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
  );
}
