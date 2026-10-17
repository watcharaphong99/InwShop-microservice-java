CREATE TABLE players_inventory (
    id                  UUID PRIMARY KEY,
    player_id           VARCHAR(128) NOT NULL,
    item_id             VARCHAR(128) NOT NULL,
    event_id            VARCHAR(128),
    removed_event_id    VARCHAR(128),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX uq_players_inventory_event_id
    ON players_inventory (event_id) WHERE event_id IS NOT NULL;

CREATE UNIQUE INDEX uq_players_inventory_removed_event_id
    ON players_inventory (removed_event_id) WHERE removed_event_id IS NOT NULL;

CREATE INDEX idx_players_inventory_player_id ON players_inventory (player_id);

CREATE TABLE inventory_event_cancels (
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
VALUES ('kafka_offset', 'inventory', 0, -1);
