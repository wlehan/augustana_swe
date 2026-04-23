import React from 'react';

export default function ScoreLedger({
  showHostLobbyModal,
  isLedgerOpen,
  setIsLedgerOpen,
  game,
  playerScores,
  ledgerRounds,
}) {
  if (showHostLobbyModal) return null;

  return (
    <div className={`score-ledger ${isLedgerOpen ? 'open' : ''}`}>
      <button type="button" className="ledger-toggle" onClick={() => setIsLedgerOpen((o) => !o)}>
        {isLedgerOpen ? 'Hide Ledger' : 'Show Ledger'}
      </button>

      <div className="scoreboard-container">
        <div className="scoreboard-header">
          <h2 className="scoreboard-title">Score Ledger</h2>
          <span className="scoreboard-status">Round {game?.currentRound || 0} / 9</span>
        </div>

        <div className="scoreboard-table">
          <div className="scoreboard-row scoreboard-row-head">
            <span>Player</span>
            {ledgerRounds.map((r) => (
              <span key={r.roundNumber}>R{r.roundNumber}</span>
            ))}
            <span>Total</span>
          </div>

          {playerScores.map((player) => (
            <div key={player.id} className="scoreboard-row">
              <span>{player.name}</span>
              {ledgerRounds.map((r) => (
                <span key={r.roundNumber}>
                  {r.perPlayer[player.gamePlayerId] !== undefined ? r.perPlayer[player.gamePlayerId] : '-'}
                </span>
              ))}
              <span>{player.total}</span>
            </div>
          ))}
        </div>

        <p className="scoreboard-footnote">
          Lowest total after 9 rounds wins. Pair in a column = 0 pts. King = 0, Two = −2.
        </p>
      </div>
    </div>
  );
}
