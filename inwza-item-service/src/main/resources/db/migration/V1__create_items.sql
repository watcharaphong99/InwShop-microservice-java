CREATE TABLE items (
    id              UUID PRIMARY KEY,
    title           VARCHAR(64) NOT NULL UNIQUE,
    price           NUMERIC(19, 4) NOT NULL CHECK (price >= 0),
    damage          INT NOT NULL CHECK (damage >= 0),
    image_url       VARCHAR(255) NOT NULL,
    usage_status    BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_items_title ON items (title);
CREATE INDEX idx_items_usage_status ON items (usage_status);
