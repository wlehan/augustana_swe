import React, { useEffect, useMemo, useRef, useState } from 'react';
import profileIcon from '../assets/profile.png';
import { getCardImage, getCardAlt } from './cardHelpers';

const PLAYER_HAND_SLOTS = [1, 2, 3, 4, 5, 6];

export default function PlayerHand({ position, playerMeta, onCardClick, cardHighlight }) {
  const cardsByPosition = useMemo(
    () => new Map((playerMeta?.cards || []).map((c) => [c.position, c])),
    [playerMeta?.cards]
  );
  const isClickable = Boolean(onCardClick);

  const prevFaceUpRef = useRef({});
  const [flippingSlots, setFlippingSlots] = useState(new Set());

  useEffect(() => {
    const prev = prevFaceUpRef.current;
    const newlyFlipped = [];
    PLAYER_HAND_SLOTS.forEach((slot) => {
      const card = cardsByPosition.get(slot);
      if (card && card.faceUp && !prev[slot]) {
        newlyFlipped.push(slot);
      }
      prev[slot] = card?.faceUp ?? false;
    });
    if (newlyFlipped.length > 0) {
      const start = setTimeout(() => setFlippingSlots(new Set(newlyFlipped)), 0);
      const end = setTimeout(() => setFlippingSlots(new Set()), 320);
      return () => {
        clearTimeout(start);
        clearTimeout(end);
      };
    }
    return undefined;
  }, [cardsByPosition]);

  return (
    <div className={`player-area ${position}`}>
      {playerMeta && (
        <div className={`score-chip ${position}`}>
          <span className="score-chip-name">{playerMeta.name}</span>
          <span className="score-chip-total">{playerMeta.total}</span>
        </div>
      )}

      {position !== 'bottom' && (
        <img src={profileIcon} className="player-profile-img" alt="player" />
      )}

      <div className="card-grid">
        {PLAYER_HAND_SLOTS.map((slot, index) => {
          const card = cardsByPosition.get(slot);
          const imageSrc = getCardImage(card);
          const hl = cardHighlight?.(slot, card);
          const isFlipping = flippingSlots.has(slot);

          return (
            <div
              key={slot}
              className={`card-slot ${card ? 'dealt-card' : ''} ${isClickable ? 'card-clickable' : ''} ${hl ? `card-hl-${hl}` : ''}`}
              style={card ? { animationDelay: `${index * 90}ms` } : undefined}
              onClick={onCardClick ? () => onCardClick(slot, card) : undefined}
            >
              {imageSrc && (
                <img
                  src={imageSrc}
                  alt={getCardAlt(card)}
                  className={`playing-card-img ${isFlipping ? 'card-flip-anim' : ''}`}
                />
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}
