-- Database: inwza_inventory

CREATE TABLE IF NOT EXISTS players_inventory (
    id                  UUID PRIMARY KEY,
    player_id           VARCHAR(128) NOT NULL,
    item_id             VARCHAR(128) NOT NULL,
    event_id            VARCHAR(128),
    removed_event_id    VARCHAR(128),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_players_inventory_event_id
    ON players_inventory (event_id) WHERE event_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_players_inventory_removed_event_id
    ON players_inventory (removed_event_id) WHERE removed_event_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_players_inventory_player_id ON players_inventory (player_id);

CREATE TABLE IF NOT EXISTS inventory_event_cancels (
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
VALUES ('kafka_offset', 'inventory', 0, -1)
ON CONFLICT (id) DO NOTHING;

-- Optional: player001 already owns Wooden Sword (for sell / inventory UI tests)
INSERT INTO players_inventory (id, player_id, item_id, event_id, removed_event_id, created_at)
VALUES (
    '55555555-5555-4555-8555-555555555501',
    'player:33333333-3333-4333-8333-333333333301',
    'item:11111111-1111-4111-8111-111111111103',
    'mock-seed-wooden-sword',
    NULL,
    NOW()
)
ON CONFLICT (id) DO NOTHING;
