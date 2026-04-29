import React from 'react';

export default function HelpModal({ isOpen, onClose }) {
  if (!isOpen) return null;

  return (
    <div className="help-overlay" onClick={onClose}>
      <div
        className="help-popup"
        onClick={(e) => e.stopPropagation()}
      >
        <h2>How to Play</h2>

        <p><strong>Goal:</strong> Finish with the lowest total score.</p>

        <h3>Setup</h3>
        <ul>
          <li>Each player has 6 cards (2 rows of 3).</li>
          <li>2 cards start face-up, 4 face-down.</li>
        </ul>

        <h3>Your Turn</h3>
        <ul>
          <li>Draw a card from the deck or discard pile.</li>
          <li>Swap it with one of your 6 cards <em>or</em> discard it.</li>
          <li>If you choose to discard, you must select a face down card from your hand and flip it.</li>
        </ul>

        <h3>Ending a Round</h3>
        <ul>
          <li>When someone's hand is fully face-up, the round ends.</li>
          <li>Everyone else gets one final turn.</li>
        </ul>

        <h3>Scoring</h3>
        <ul>
          <li>Number cards = face value</li>
          <li>Ace = 1 point</li>
          <li>King = 0 points</li>
          <li>Other face cards = 10 points</li>
          <li>Pairs in a column = 0 points</li>
          <li>Two = -2 points</li>
          <li>Pair of 2s in a column = 0 points</li>
        </ul>

        <h4>Ending the Game</h4>
        <ul>
          <li>After 9 rounds, the player with the lowest total score wins.</li>
        </ul>

        <p><strong>Tip:</strong> Try to match columns and keep high cards out.</p>
        <button
          type="button"
          className="help-close-btn"
          onClick={onClose}
        >
          Close
        </button>
      </div>
    </div>
  );
}