CREATE TABLE round_scores (
    id              BIGINT IDENTITY(1,1) PRIMARY KEY,
    round_id        BIGINT NOT NULL,
    game_player_id  BIGINT NOT NULL,
    score           INT    NOT NULL,

    CONSTRAINT FK_rs_round FOREIGN KEY (round_id)
        REFERENCES rounds(round_id) ON DELETE CASCADE,

    CONSTRAINT FK_rs_gp FOREIGN KEY (game_player_id)
        REFERENCES game_players(game_player_id) ON DELETE NO ACTION,

    CONSTRAINT UQ_rs_round_player UNIQUE (round_id, game_player_id)
);

ALTER TABLE rounds ADD current_draw_source NVARCHAR(10) NULL;
ALTER TABLE rounds ADD CONSTRAINT CK_rounds_draw_source
    CHECK (current_draw_source IN ('STOCK', 'DISCARD') OR current_draw_source IS NULL);
