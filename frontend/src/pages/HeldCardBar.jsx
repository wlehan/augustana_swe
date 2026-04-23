import React from 'react';
import { getCardImage, getCardAlt } from './cardHelpers';

export default function HeldCardBar({
  myHeldCard,
  pendingDiscard,
  currentDrawSource,
  actionBusy,
  handleDiscardClick,
  setPendingDiscard,
}) {
  if (!myHeldCard) return null;

  return (
    <div className="held-card-bar">
      <div className="held-card-label">In hand</div>
      <div className="held-card-preview">
        <div className="card-slot dealt-card held-card-slot">
          <img
            src={getCardImage({ ...myHeldCard, revealedToViewer: true, faceUp: true })}
            alt={getCardAlt({ ...myHeldCard, revealedToViewer: true, faceUp: true })}
            className="playing-card-img"
          />
        </div>
      </div>
      <div className="held-card-instructions">
        {pendingDiscard
          ? 'Click a face-down card in your grid to flip'
          : 'Click any card in your grid to swap'}
      </div>
      {currentDrawSource === 'STOCK' && !pendingDiscard && (
        <button
          type="button"
          className="discard-btn"
          onClick={handleDiscardClick}
          disabled={actionBusy}
        >
          Discard &amp; Flip
        </button>
      )}
      {pendingDiscard && (
        <button type="button" className="discard-cancel-btn" onClick={() => setPendingDiscard(false)}>
          Cancel
        </button>
      )}
    </div>
  );
}
