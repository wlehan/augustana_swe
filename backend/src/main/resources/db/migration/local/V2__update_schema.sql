ALTER TABLE rounds ADD status NVARCHAR(20) NOT NULL DEFAULT 'SETUP';
ALTER TABLE rounds ADD current_turn_game_player_id BIGINT NULL;
ALTER TABLE rounds ADD dealer_seat INT NULL;
ALTER TABLE rounds ADD final_turn_triggered_by_game_player_id BIGINT NULL;

ALTER TABLE rounds
ADD CONSTRAINT CK_rounds_status
CHECK (status IN ('SETUP', 'ACTIVE', 'FINAL_TURNS', 'SCORED'));

ALTER TABLE rounds
ADD CONSTRAINT FK_rounds_current_turn_game_player
FOREIGN KEY (current_turn_game_player_id) REFERENCES game_players(game_player_id);

ALTER TABLE rounds
ADD CONSTRAINT FK_rounds_final_turn_triggered_by_game_player
FOREIGN KEY (final_turn_triggered_by_game_player_id) REFERENCES game_players(game_player_id);

ALTER TABLE cards ALTER COLUMN owner_game_player_id BIGINT NULL;
ALTER TABLE cards ALTER COLUMN position INT NULL;

ALTER TABLE cards ADD pile NVARCHAR(20) NOT NULL DEFAULT 'GRID';
ALTER TABLE cards ADD draw_order INT NULL;

ALTER TABLE cards
ADD CONSTRAINT CK_cards_pile
CHECK (pile IN ('GRID', 'DRAW', 'DISCARD', 'HAND'));