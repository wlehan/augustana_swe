-- Migration: seed the tutorial bot user
-- Run this once against your database (or include it in your Flyway/Liquibase scripts).
--
-- The password_hash value is a deliberate placeholder that will never match any
-- real BCrypt comparison — the bot never logs in through the auth flow.

IF NOT EXISTS (
    SELECT 1 FROM users WHERE username = 'tutorial_bot'
)
BEGIN
    INSERT INTO users (username, password_hash, email)
    VALUES (
        'tutorial_bot',
        '$2a$10$TUTORIAL_BOT_PLACEHOLDER_HASH_THAT_NEVER_MATCHES',
        NULL
    );
END
