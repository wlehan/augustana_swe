import cardBack from '../assets/cards/card_back.png';
import aceclubs from '../assets/cards/clubs/aceclubs.png';
import twoclubs from '../assets/cards/clubs/2clubs.png';
import threeclubs from '../assets/cards/clubs/3clubs.png';
import fourclubs from '../assets/cards/clubs/4clubs.png';
import fiveclubs from '../assets/cards/clubs/5clubs.png';
import sixclubs from '../assets/cards/clubs/6clubs.png';
import sevenclubs from '../assets/cards/clubs/7clubs.png';
import eightclubs from '../assets/cards/clubs/8clubs.png';
import nineclubs from '../assets/cards/clubs/9clubs.png';
import tenclubs from '../assets/cards/clubs/10clubs.png';
import jackclubs from '../assets/cards/clubs/jackclubs.png';
import queenclubs from '../assets/cards/clubs/queenclubs.png';
import kingclubs from '../assets/cards/clubs/kingclubs.png';

import acediamonds from '../assets/cards/diamonds/acediamonds.png';
import twodiamonds from '../assets/cards/diamonds/2diamonds.png';
import threediamonds from '../assets/cards/diamonds/3diamonds.png';
import fourdiamonds from '../assets/cards/diamonds/4diamonds.png';
import fivediamonds from '../assets/cards/diamonds/5diamonds.png';
import sixdiamonds from '../assets/cards/diamonds/6diamonds.png';
import sevendiamonds from '../assets/cards/diamonds/7diamonds.png';
import eightdiamonds from '../assets/cards/diamonds/8diamonds.png';
import ninediamonds from '../assets/cards/diamonds/9diamonds.png';
import tendiamonds from '../assets/cards/diamonds/10diamonds.png';
import jackdiamonds from '../assets/cards/diamonds/jackdiamonds.png';
import queendiamonds from '../assets/cards/diamonds/queendiamonds.png';
import kingdiamonds from '../assets/cards/diamonds/kingdiamonds.png';

import acehearts from '../assets/cards/hearts/acehearts.png';
import twohearts from '../assets/cards/hearts/2hearts.png';
import threehearts from '../assets/cards/hearts/3hearts.png';
import fourhearts from '../assets/cards/hearts/4hearts.png';
import fivehearts from '../assets/cards/hearts/5hearts.png';
import sixhearts from '../assets/cards/hearts/6hearts.png';
import sevenhearts from '../assets/cards/hearts/7hearts.png';
import eighthearts from '../assets/cards/hearts/8hearts.png';
import ninehearts from '../assets/cards/hearts/9hearts.png';
import tenhearts from '../assets/cards/hearts/10hearts.png';
import jackhearts from '../assets/cards/hearts/jackhearts.png';
import queenhearts from '../assets/cards/hearts/queenhearts.png';
import kinghearts from '../assets/cards/hearts/kinghearts.png';

import acespades from '../assets/cards/spades/acespades.png';
import twospades from '../assets/cards/spades/2spades.png';
import threespades from '../assets/cards/spades/3spades.png';
import fourspades from '../assets/cards/spades/4spades.png';
import fivespades from '../assets/cards/spades/5spades.png';
import sixspades from '../assets/cards/spades/6spades.png';
import sevenspades from '../assets/cards/spades/7spades.png';
import eightspades from '../assets/cards/spades/8spades.png';
import ninespades from '../assets/cards/spades/9spades.png';
import tenspades from '../assets/cards/spades/10spades.png';
import jackspades from '../assets/cards/spades/jackspades.png';
import queenspades from '../assets/cards/spades/queenspades.png';
import kingspades from '../assets/cards/spades/kingspades.png';

const CARD_IMAGES = {
  CLUBS: {
    ACE: aceclubs,
    TWO: twoclubs,
    THREE: threeclubs,
    FOUR: fourclubs,
    FIVE: fiveclubs,
    SIX: sixclubs,
    SEVEN: sevenclubs,
    EIGHT: eightclubs,
    NINE: nineclubs,
    TEN: tenclubs,
    JACK: jackclubs,
    QUEEN: queenclubs,
    KING: kingclubs,
  },
  DIAMONDS: {
    ACE: acediamonds,
    TWO: twodiamonds,
    THREE: threediamonds,
    FOUR: fourdiamonds,
    FIVE: fivediamonds,
    SIX: sixdiamonds,
    SEVEN: sevendiamonds,
    EIGHT: eightdiamonds,
    NINE: ninediamonds,
    TEN: tendiamonds,
    JACK: jackdiamonds,
    QUEEN: queendiamonds,
    KING: kingdiamonds,
  },
  HEARTS: {
    ACE: acehearts,
    TWO: twohearts,
    THREE: threehearts,
    FOUR: fourhearts,
    FIVE: fivehearts,
    SIX: sixhearts,
    SEVEN: sevenhearts,
    EIGHT: eighthearts,
    NINE: ninehearts,
    TEN: tenhearts,
    JACK: jackhearts,
    QUEEN: queenhearts,
    KING: kinghearts,
  },
  SPADES: {
    ACE: acespades,
    TWO: twospades,
    THREE: threespades,
    FOUR: fourspades,
    FIVE: fivespades,
    SIX: sixspades,
    SEVEN: sevenspades,
    EIGHT: eightspades,
    NINE: ninespades,
    TEN: tenspades,
    JACK: jackspades,
    QUEEN: queenspades,
    KING: kingspades,
  },
};

export function getCardImage(card) {
  if (!card) return null;
  if (!card.faceUp && !card.revealedToViewer) return cardBack;
  return CARD_IMAGES?.[card.suit]?.[card.rank] || null;
}

export function getCardAlt(card) {
  if (!card) return 'Card slot';
  if (!card.faceUp && !card.revealedToViewer) return 'Face-down card';
  return `${card.rank} of ${card.suit}`;
}

export { cardBack };
