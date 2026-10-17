-- Database: inwza_player
-- Password for all mock users: 123456 (BCrypt)

CREATE TABLE IF NOT EXISTS players (
    id          UUID PRIMARY KEY,
    email       VARCHAR(255) NOT NULL UNIQUE,
    username    VARCHAR(64) NOT NULL UNIQUE,
    password    TEXT NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS player_roles (
    player_id   UUID NOT NULL REFERENCES players(id) ON DELETE CASCADE,
    role_title  VARCHAR(64) NOT NULL,
    role_code   INT NOT NULL,
    PRIMARY KEY (player_id, role_code)
);

CREATE TABLE IF NOT EXISTS player_wallets (
    player_id   VARCHAR(128) PRIMARY KEY,
    balance     NUMERIC(19, 4) NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS player_transactions (
    id          UUID PRIMARY KEY,
    player_id   VARCHAR(128) NOT NULL,
    amount      NUMERIC(19, 4) NOT NULL,
    event_id    VARCHAR(128),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_player_transactions_event_id
    ON player_transactions (event_id) WHERE event_id IS NOT NULL;

CREATE TABLE IF NOT EXISTS player_tx_cancels (
    event_id    VARCHAR(128) PRIMARY KEY,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS kafka_consumer_offset (
    id              VARCHAR(32) PRIMARY KEY,
    topic           VARCHAR(64) NOT NULL,
    partition_id    INT NOT NULL DEFAULT 0,
    offset_value    BIGINT NOT NULL DEFAULT -1
);

INSERT INTO kafka_consumer_offset (id, topic, partition_id, offset_value)
VALUES ('kafka_offset', 'player', 0, -1)
ON CONFLICT (id) DO NOTHING;

CREATE INDEX IF NOT EXISTS idx_player_transactions_player_id ON player_transactions (player_id);

-- Fixed UUIDs for stable player:... ids in API / inventory
INSERT INTO players (id, email, username, password, created_at, updated_at)
VALUES
    ('33333333-3333-4333-8333-333333333301', 'player001@Iwpshop.com', 'Player001',
     '$2b$10$jC9uIk.EhrMFsyo2AKyTxuZPHch3XVmqFh.aUvlLzMrtLaB5MsANi', NOW(), NOW()),
    ('33333333-3333-4333-8333-333333333302', 'player002@Iwpshop.com', 'Player002',
     '$2b$10$jC9uIk.EhrMFsyo2AKyTxuZPHch3XVmqFh.aUvlLzMrtLaB5MsANi', NOW(), NOW()),
    ('33333333-3333-4333-8333-333333333303', 'player003@Iwpshop.com', 'Player003',
     '$2b$10$jC9uIk.EhrMFsyo2AKyTxuZPHch3XVmqFh.aUvlLzMrtLaB5MsANi', NOW(), NOW()),
    ('33333333-3333-4333-8333-333333333304', 'admin001@Iwpshop.com', 'Admin001',
     '$2b$10$jC9uIk.EhrMFsyo2AKyTxuZPHch3XVmqFh.aUvlLzMrtLaB5MsANi', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO player_roles (player_id, role_title, role_code)
VALUES
    ('33333333-3333-4333-8333-333333333301', 'player', 0),
    ('33333333-3333-4333-8333-333333333302', 'player', 0),
    ('33333333-3333-4333-8333-333333333303', 'player', 0),
    ('33333333-3333-4333-8333-333333333304', 'player', 0),
    ('33333333-3333-4333-8333-333333333304', 'admin', 1)
ON CONFLICT DO NOTHING;

INSERT INTO player_wallets (player_id, balance)
VALUES
    ('player:33333333-3333-4333-8333-333333333301', 1000),
    ('player:33333333-3333-4333-8333-333333333302', 1000),
    ('player:33333333-3333-4333-8333-333333333303', 1000),
    ('player:33333333-3333-4333-8333-333333333304', 1000)
ON CONFLICT (player_id) DO UPDATE SET balance = EXCLUDED.balance;

INSERT INTO player_transactions (id, player_id, amount, event_id, created_at)
VALUES
    ('44444444-4444-4444-8444-444444444401', 'player:33333333-3333-4333-8333-333333333301', 1000, NULL, NOW()),
    ('44444444-4444-4444-8444-444444444402', 'player:33333333-3333-4333-8333-333333333302', 1000, NULL, NOW()),
    ('44444444-4444-4444-8444-444444444403', 'player:33333333-3333-4333-8333-333333333303', 1000, NULL, NOW()),
    ('44444444-4444-4444-8444-444444444404', 'player:33333333-3333-4333-8333-333333333304', 1000, NULL, NOW())
ON CONFLICT (id) DO NOTHING;
