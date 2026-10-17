-- Database: inwza_item (pgAdmin Query Tool)

CREATE TABLE IF NOT EXISTS items (
    id              UUID PRIMARY KEY,
    title           VARCHAR(64) NOT NULL UNIQUE,
    price           NUMERIC(19, 4) NOT NULL CHECK (price >= 0),
    damage          INT NOT NULL CHECK (damage >= 0),
    image_url       VARCHAR(255) NOT NULL,
    usage_status    BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_items_title ON items (title);
CREATE INDEX IF NOT EXISTS idx_items_usage_status ON items (usage_status);

INSERT INTO items (id, title, price, damage, image_url, usage_status, created_at, updated_at)
VALUES
    ('11111111-1111-4111-8111-111111111101', 'Diamond Sword', 1000, 100, 'https://i.imgur.com/1Y8tQZM.png', TRUE, NOW(), NOW()),
    ('11111111-1111-4111-8111-111111111102', 'Iron Sword', 500, 50, 'https://i.imgur.com/1Y8tQZM.png', TRUE, NOW(), NOW()),
    ('11111111-1111-4111-8111-111111111103', 'Wooden Sword', 100, 20, 'https://i.imgur.com/1Y8tQZM.png', TRUE, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;
