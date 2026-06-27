CREATE TABLE IF NOT EXISTS users (
    hh_user_id VARCHAR(50) PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS user_preferences (
    id BIGSERIAL PRIMARY KEY,
    hh_user_id VARCHAR(50) NOT NULL UNIQUE,
    city VARCHAR(100) DEFAULT 'Челябинск',
    keywords VARCHAR(255) DEFAULT '',
    CONSTRAINT fk_user_preferences_user
        FOREIGN KEY (hh_user_id)
        REFERENCES users(hh_user_id)
        ON DELETE CASCADE
);