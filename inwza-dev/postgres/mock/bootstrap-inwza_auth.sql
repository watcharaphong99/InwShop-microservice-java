-- Database: inwza_auth

CREATE TABLE IF NOT EXISTS roles (
    id          UUID PRIMARY KEY,
    title       VARCHAR(64) NOT NULL,
    code        INT NOT NULL UNIQUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS credentials (
    id              UUID PRIMARY KEY,
    player_id       VARCHAR(128) NOT NULL,
    role_code       INT NOT NULL,
    access_token    TEXT NOT NULL,
    refresh_token   TEXT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_credentials_player_id ON credentials (player_id);
CREATE INDEX IF NOT EXISTS idx_credentials_refresh_token ON credentials (refresh_token);
CREATE UNIQUE INDEX IF NOT EXISTS idx_credentials_access_token ON credentials (access_token);

INSERT INTO roles (id, title, code, created_at)
VALUES
    ('22222222-2222-4222-8222-222222222201', 'player', 0, NOW()),
    ('22222222-2222-4222-8222-222222222202', 'admin', 1, NOW())
ON CONFLICT (id) DO NOTHING;
