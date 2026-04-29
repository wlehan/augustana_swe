import { CARD_IMAGES } from './cardImages';
import cardBack from '../assets/cards/card_back.png';

export function getCardImage(card) {
  if (!card) return null;
  if (!card.faceUp && !card.revealedToViewer) return cardBack;
  return CARD_IMAGES?.[card.suit]?.[card.rank] || null;
}

export function formatCardPart(value) {
  if (!value) return '';
  return value.charAt(0).toUpperCase() + value.slice(1).toLowerCase();
}

export function getCardName(card) {
  if (!card) return 'Card';
  if (!card.faceUp && !card.revealedToViewer) return 'Face-down card';
  return `${formatCardPart(card.rank)} of ${formatCardPart(card.suit)}`;
}

export function getCardAlt(card) {
  if (!card) return 'Card slot';
  return getCardName(card);
}

export function normalizeId(v) {
  return v === null || v === undefined ? '' : String(v);
}

export const PLAYER_HAND_SLOTS = [1, 2, 3, 4, 5, 6];

export function getOpponentPositionOrder(opponentCount) {
  if (opponentCount <= 1) return ['top'];
  if (opponentCount === 2) return ['left', 'right'];
  return ['left', 'top', 'right'];
}