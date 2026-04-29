import React from 'react';
import PlayerHand from './PlayerHand';
import { getCardImage, getCardAlt } from './gameUtils';
import cardBack from '../assets/cards/card_back.png';

export default function GameTable({
  playersByPosition,
  isMyTurn,
  myHeldCard,
  game,
  handleDrawFromStock,
  handleDrawFromDiscard,
  handleFlipInitial,
  handleSwap,
  handleSelectFlip,
  isSetupPhase,
  myInitialFlips,
  pendingDiscard,
  myCardHighlight
}) {
  const discardImage = getCardImage(game?.round?.discardTop);

  return (
    <div className="table-layout">
      <PlayerHand position="top" playerMeta={playersByPosition.top} />
      <PlayerHand position="left" playerMeta={playersByPosition.left} />
      <div className="player-area center">
        <div className="deck-stack">
          <div
            className={`card-slot dealt-card center-pile-card ${isMyTurn && !myHeldCard ? 'pile-active' : ''}`}
            onClick={isMyTurn && !myHeldCard ? handleDrawFromStock : undefined}
            title={isMyTurn && !myHeldCard ? 'Draw from stock' : ''}
          >
            <img src={cardBack} alt="Draw pile" className="playing-card-img" />
            {game?.round?.drawPileCount !== undefined && (
              <div className="pile-count-badge">{game.round.drawPileCount}</div>
            )}
          </div>
          <div
            className={`card-slot dealt-card center-pile-card discard-pile ${isMyTurn && !myHeldCard ? 'pile-active' : ''}`}
            onClick={isMyTurn && !myHeldCard ? handleDrawFromDiscard : undefined}
            title={isMyTurn && !myHeldCard ? 'Draw from discard' : ''}
          >
            {discardImage ? (
              <img src={discardImage} alt={getCardAlt(game?.round?.discardTop)} className="playing-card-img" />
            ) : (
              <span className="discard-placeholder">Discard</span>
            )}
          </div>
        </div>
      </div>

      <PlayerHand position="right" playerMeta={playersByPosition.right} />
      <PlayerHand
        position="bottom"
        playerMeta={playersByPosition.bottom}
        onCardClick={
          (isSetupPhase && myInitialFlips < 2)
            ? (slot) => handleFlipInitial(slot)
            : (isMyTurn && myHeldCard && !pendingDiscard)
            ? (slot) => handleSwap(slot)
            : (isMyTurn && myHeldCard && pendingDiscard)
            ? handleSelectFlip
            : null
        }
        cardHighlight={myCardHighlight}
      />
    </div>
  );
}