import React from 'react';
import './GamePage.css'; // Make sure this path is correct!
import gearIcon from '../assets/Icon.png';
import profileIcon from '../assets/profile.png';

function PlayerHand({ position }) {
  const slots = [1, 2, 3, 4, 5, 6];
  return (
    <div className={`player-area ${position}`}>
      {position !== 'bottom' && (
      <img src = {profileIcon} className = "player-profile-img" alt="player" />
      )}
      <div className="card-grid">
        {slots.map((slot) => (
          <div key={slot} className="card-slot"></div>
        ))}
      </div>
    </div>
  );
}

export default function GamePage() { // Added 'export default'
  return (
    <div className="game-container">       
      <img src={gearIcon} className="ui-icon settings-gear" alt="settings" />
      <div className="jump-end-btn">Jump to end</div>
      <img src={profileIcon} className="ui-icon top-profile" alt="my profile" />

      <PlayerHand position="top" />
      <PlayerHand position="left" />
      <div className="player-area center">
        <div className="deck-stack" >
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