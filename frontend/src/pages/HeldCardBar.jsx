import React from 'react';
import { getCardImage, getCardName } from './gameUtils';

/**
 * Shows the current viewer's held card and the legal resolve actions for it.
 */
export default function HeldCardBar({
  myHeldCard,
  pendingDiscard,
  canDiscardHeldCard,
  actionBusy,
  handleDiscardClick,
  onCancelDiscard
}) {
  if (!myHeldCard) {
    return null;
  }

  const heldCardImage = getCardImage(myHeldCard);
  const heldCardName = getCardName(myHeldCard);

  return (
    <div className="held-card-bar" aria-live="polite">
      <div className="held-card-preview">
        <div className="card-slot dealt-card held-card-slot">
          {heldCardImage && (
            <img
              src={heldCardImage}
              alt={`Held card: ${heldCardName}`}
              className="playing-card-img"
            />
          )}
        </div>
      </div>
      <div className="held-card-copy">
        <div className="held-card-label">Card in hand</div>
        <div className="held-card-name">{heldCardName}</div>
        <div className="held-card-instructions">
          {pendingDiscard
            ? 'Choose a face-down card to flip.'
            : canDiscardHeldCard
            ? 'Swap it with one of your cards, or discard it.'
            : 'Swap it with one of your cards.'}
        </div>
      </div>
      {canDiscardHeldCard && !pendingDiscard && (
        <button
          type="button"
          className="discard-btn"
          onClick={handleDiscardClick}
          disabled={actionBusy}
        >
          Discard
        </button>
      )}
      {pendingDiscard && (
        <button
          type="button"
          className="discard-cancel-btn"
          onClick={onCancelDiscard}
          disabled={actionBusy}
        >
          Cancel
        </button>
      )}
    </div>
  );
}
