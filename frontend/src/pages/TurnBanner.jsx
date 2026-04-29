import React from 'react';

export default function TurnBanner({
  isActivePlaying,
  isMyTurn,
  roundStatus,
  finalTriggerPlayer,
  currentTurnPlayer,
  myHeldCard,
  pendingDiscard,
  isSetupPhase,
  game,
  myInitialFlips
}) {
  if (isActivePlaying) {
    return (
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
    );
  }

  if (isSetupPhase) {
    return (
      <div className="turn-banner setup-banner">
        Round {game?.currentRound} — Flip 2 cards to start &nbsp;
        {isActivePlaying ? null : (
          <span className="setup-progress">{myInitialFlips}/2 flipped</span>
        )}
      </div>
    );
  }

  return null;
}