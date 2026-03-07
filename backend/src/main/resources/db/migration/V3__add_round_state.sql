-- Adds persistent round/card state needed for six-card golf gameplay

/* =========================
   ROUNDS: add gameplay state
   ========================= */

ALTER TABLE rounds
ADD status NVARCHAR(20) NOT NULL
    CONSTRAINT DF_rounds_status DEFAULT 'SETUP';

ALTER TABLE rounds
ADD current_turn_game_player_id BIGINT NULL;

ALTER TABLE rounds
ADD dealer_seat INT NULL;

ALTER TABLE rounds
ADD final_turn_triggered_by_game_player_id BIGINT NULL;

ALTER TABLE rounds
ADD CONSTRAINT CK_rounds_status
    CHECK (status IN ('SETUP', 'ACTIVE', 'FINAL_TURNS', 'SCORED'));

ALTER TABLE rounds
ADD CONSTRAINT FK_rounds_current_turn_game_player
    FOREIGN KEY (current_turn_game_player_id)
    REFERENCES game_players(game_player_id)
    ON DELETE NO ACTION;

ALTER TABLE rounds
ADD CONSTRAINT FK_rounds_final_turn_triggered_by_game_player
    FOREIGN KEY (final_turn_triggered_by_game_player_id)
    REFERENCES game_players(game_player_id)
    ON DELETE NO ACTION;


/* =========================
   CARDS: allow deck/discard/hand/grid state
   ========================= */

ALTER TABLE cards
ALTER COLUMN owner_game_player_id BIGINT NULL;

ALTER TABLE cards
ALTER COLUMN position INT NULL;

ALTER TABLE cards
ADD pile NVARCHAR(20) NOT NULL
    CONSTRAINT DF_cards_pile DEFAULT 'GRID';

ALTER TABLE cards
ADD draw_order INT NULL;

ALTER TABLE cards
ADD CONSTRAINT CK_cards_pile
    CHECK (pile IN ('GRID', 'DRAW', 'DISCARD', 'HAND'));


/* =========================
   CARD uniqueness rules
   ========================= */

/*
Current constraint from V1:
    uq_card_position UNIQUE (round_id, owner_game_player_id, position)

Because owner_game_player_id and position are becoming nullable, that old
constraint is no longer enough by itself for all gameplay states. We’ll replace it
with a filtered unique index that only applies to cards in a player grid.
*/

ALTER TABLE cards
DROP CONSTRAINT uq_card_position;

CREATE UNIQUE INDEX UX_cards_grid_position
ON cards (round_id, owner_game_player_id, position)
WHERE pile = 'GRID'
  AND owner_game_player_id IS NOT NULL
  AND position IS NOT NULL;


/* =========================
   Helpful lookup indexes
   ========================= */

CREATE INDEX IX_rounds_game_status
ON rounds (game_id, status);

CREATE INDEX IX_cards_round_pile
ON cards (round_id, pile);

CREATE INDEX IX_cards_round_draw_order
ON cards (round_id, draw_order);

CREATE INDEX IX_cards_owner_round
ON cards (owner_game_player_id, round_id);