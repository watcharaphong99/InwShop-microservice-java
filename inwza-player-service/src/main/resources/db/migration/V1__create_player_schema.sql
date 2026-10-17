CREATE TABLE players (
    id          UUID PRIMARY KEY,
    email       VARCHAR(255) NOT NULL UNIQUE,
    username    VARCHAR(64) NOT NULL UNIQUE,
    password    TEXT NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE player_roles (
    player_id   UUID NOT NULL REFERENCES players(id) ON DELETE CASCADE,
    role_title  VARCHAR(64) NOT NULL,
    role_code   INT NOT NULL,
    PRIMARY KEY (player_id, role_code)
);

CREATE TABLE player_wallets (
    player_id   VARCHAR(128) PRIMARY KEY,
    balance     NUMERIC(19, 4) NOT NULL DEFAULT 0
);

CREATE TABLE player_transactions (
    id          UUID PRIMARY KEY,
    player_id   VARCHAR(128) NOT NULL,
    amount      NUMERIC(19, 4) NOT NULL,
    event_id    VARCHAR(128),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX uq_player_transactions_event_id ON player_transactions (event_id) WHERE event_id IS NOT NULL;

CREATE TABLE player_tx_cancels (
    event_id    VARCHAR(128) PRIMARY KEY,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE kafka_consumer_offset (
    id              VARCHAR(32) PRIMARY KEY,
    topic           VARCHAR(64) NOT NULL,
    partition_id    INT NOT NULL DEFAULT 0,
    offset_value    BIGINT NOT NULL DEFAULT -1
);

INSERT INTO kafka_consumer_offset (id, topic, partition_id, offset_value)
VALUES ('kafka_offset', 'player', 0, -1);

CREATE INDEX idx_player_transactions_player_id ON player_transactions (player_id);
