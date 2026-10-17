-- Database: inwza_payment

CREATE TABLE IF NOT EXISTS order_workflows (
    id          VARCHAR(64) PRIMARY KEY,
    player_id   VARCHAR(128) NOT NULL,
    action      VARCHAR(16) NOT NULL,
    status      VARCHAR(32) NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    steps       JSONB NOT NULL DEFAULT '[]'::jsonb
);

CREATE INDEX IF NOT EXISTS idx_order_workflows_status_updated ON order_workflows (status, updated_at);

CREATE TABLE IF NOT EXISTS kafka_consumer_offset (
    id              VARCHAR(32) PRIMARY KEY,
    topic           VARCHAR(64) NOT NULL,
    partition_id    INT NOT NULL DEFAULT 0,
    offset_value    BIGINT NOT NULL DEFAULT -1
);

INSERT INTO kafka_consumer_offset (id, topic, partition_id, offset_value)
VALUES ('kafka_offset', 'payment', 0, -1)
ON CONFLICT (id) DO NOTHING;
