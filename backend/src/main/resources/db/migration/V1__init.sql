-- USERS
CREATE TABLE users (
    user_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(100) NOT NULL UNIQUE,
    password_hash NVARCHAR(255) NOT NULL,
    email NVARCHAR(255),
    created_at DATETIME2 DEFAULT SYSDATETIME()
);

-- GAMES
CREATE TABLE games (
    game_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    game_code NVARCHAR(20) NOT NULL UNIQUE,
    status NVARCHAR(20) NOT NULL
        CHECK (status IN ('WAITING','IN_PROGRESS','COMPLETED')),
    max_players INT NOT NULL,
    current_round INT DEFAULT 0,
    created_at DATETIME2 DEFAULT SYSDATETIME()
);

-- GAME PLAYERS (Join table)
CREATE TABLE game_players (
    game_player_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    game_id BIGINT NOT NULL,
    seat_number INT NOT NULL,
    total_score INT DEFAULT 0,

    CONSTRAINT fk_gp_user FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_gp_game FOREIGN KEY (game_id)
        REFERENCES games(game_id)
        ON DELETE CASCADE,

    CONSTRAINT uq_game_seat UNIQUE (game_id, seat_number)
);

-- ROUNDS
CREATE TABLE rounds (
    round_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    game_id BIGINT NOT NULL,
    round_number INT NOT NULL,
    ended_at DATETIME2 NULL,

    CONSTRAINT fk_round_game FOREIGN KEY (game_id)
        REFERENCES games(game_id)
        ON DELETE CASCADE,

    CONSTRAINT uq_game_round UNIQUE (game_id, round_number)
);

-- CARDS
CREATE TABLE cards (
    card_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    round_id BIGINT NOT NULL,
    owner_game_player_id BIGINT NOT NULL,
    suit NVARCHAR(20) NOT NULL,
    rank NVARCHAR(10) NOT NULL,
    position INT NOT NULL,
    is_face_up BIT DEFAULT 0,

    CONSTRAINT fk_card_round FOREIGN KEY (round_id)
        REFERENCES rounds(round_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_card_owner FOREIGN KEY (owner_game_player_id)
        REFERENCES game_players(game_player_id)
        ON DELETE NO ACTION,

    CONSTRAINT uq_card_position UNIQUE (round_id, owner_game_player_id, position)
);