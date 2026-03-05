import React, { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import './Gamepage.css';
import gearIcon from '../assets/Icon.png';
import profileIcon from '../assets/profile.png';
import { getGame } from '../services/gameApi';

function PlayerHand({ position }) {
  const slots = [1, 2, 3, 4, 5, 6];
  return (
    <div className={`player-area ${position}`}>
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
    } catch {
      // fallback if clipboard blocked
      window.prompt('Copy this code:', game.gameCode);
    }
  };

  return (
    <div className="game-container">
      {/* Lobby banner overlay */}
      <div className="lobby-banner">
        <div className="lobby-banner-left">
          <div className="lobby-label">Game code</div>
          <div className="lobby-code">{game?.gameCode || '------'}</div>
        </div>
        <button type="button" className="lobby-copy-btn" onClick={onCopyCode} disabled={!game?.gameCode}>
          Copy
        </button>
      </div>

      {errorMsg && <div className="lobby-error">{errorMsg}</div>}

      <img src={gearIcon} className="ui-icon settings-gear" alt="settings" />
      <div className="jump-end-btn">Jump to end</div>
      <img src={profileIcon} className="ui-icon top-profile" alt="my profile" />

      <PlayerHand position="top" />
      <PlayerHand position="left" />
      <div className="player-area center">
        <div className="deck-stack">
          <div className="card-slot">Deck</div>
          <div className="card-slot">Discard</div>
        </div>
      </div>
      <PlayerHand position="right" />
      <PlayerHand position="bottom" />

      <div className="help-button">?</div>

      <div className="scoreboard-container">
        <h2 className="scoreboard-title">Scoreboard</h2>
        {/* score data will go here */}
      </div>
    </div>
  );
}