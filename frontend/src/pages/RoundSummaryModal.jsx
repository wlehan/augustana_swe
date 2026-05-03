import React from 'react';

/**
 * Between-round dialog that shows the scores from the round that just ended.
 */
export default function RoundSummaryModal({ showRoundSummary, roundSummaryData, onClose }) {
  if (!showRoundSummary || !roundSummaryData) return null;

  return (
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
          onClick={onClose}
        >
          Continue
        </button>
      </div>
    </div>
  );
}
