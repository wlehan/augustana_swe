INSERT INTO users (username, password_hash, email)
SELECT 'tutorial_bot',
       '$2a$10$TUTORIAL_BOT_PLACEHOLDER_HASH_THAT_NEVER_MATCHES',
       NULL
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE username = 'tutorial_bot'
);
