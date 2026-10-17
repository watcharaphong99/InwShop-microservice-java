CREATE TABLE roles (
    id          UUID PRIMARY KEY,
    title       VARCHAR(64) NOT NULL,
    code        INT NOT NULL UNIQUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE credentials (
    id              UUID PRIMARY KEY,
    player_id       VARCHAR(128) NOT NULL,
    role_code       INT NOT NULL,
    access_token    TEXT NOT NULL,
    refresh_token   TEXT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_credentials_player_id ON credentials (player_id);
CREATE INDEX idx_credentials_refresh_token ON credentials (refresh_token);
CREATE UNIQUE INDEX idx_credentials_access_token ON credentials (access_token);
