import React from 'react';
import './GameOverOverlay.css';

export default function GameOverOverlay({ isGameComplete, playerScores }) {
  if (!isGameComplete) return null;

  return (
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
          onClick={() => {
            window.location.href = '/game-selection';
          }}
        >
          Play Again
        </button>
      </div>
    </div>
  );
}
