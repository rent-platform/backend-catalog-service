CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE categories (
    id              BIGSERIAL    PRIMARY KEY,
    category_name   VARCHAR(100) NOT NULL,
    slug            VARCHAR(120) NOT NULL,
    parent_id       BIGINT       REFERENCES categories(id) ON DELETE SET NULL,
    sort_order      INT          NOT NULL DEFAULT 0,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);

CREATE UNIQUE INDEX categories_slug_uq
    ON categories(slug)
        WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX categories_parent_name_uq
    ON categories(parent_id, category_name)
        WHERE deleted_at IS NULL;

CREATE INDEX categories_parent_id_idx
    ON categories(parent_id);

CREATE INDEX categories_sort_order_idx
    ON categories(sort_order);

CREATE INDEX categories_deleted_at_idx
    ON categories(deleted_at);

CREATE TABLE items (
    id                 UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id           UUID          NOT NULL,
    category_id        BIGINT        NOT NULL REFERENCES categories(id),
    title              VARCHAR(200)  NOT NULL,
    item_description   TEXT,
    price_per_day      DECIMAL(10,2),
    price_per_hour     DECIMAL(10,2),
    deposit_amount     DECIMAL(10,2) NOT NULL DEFAULT 0,
    city               VARCHAR(100)  NOT NULL,
    pickup_location    TEXT,
    status             VARCHAR(20)   NOT NULL DEFAULT 'DRAFT'
        CHECK (status IN ('DRAFT', 'MODERATION', 'ACTIVE', 'REJECTED', 'ARCHIVED')),
    moderation_comment TEXT,
    views_count        INT         NOT NULL DEFAULT 0,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at         TIMESTAMPTZ,
    CHECK (price_per_day IS NOT NULL OR
        price_per_hour IS NOT NULL),
    CHECK (price_per_day IS NULL OR
        price_per_day >= 0),
    CHECK (price_per_hour IS NULL OR
        price_per_hour >= 0),
    CHECK (deposit_amount >= 0),
    CHECK (views_count >= 0)
);

CREATE INDEX items_owner_id_idx
    ON items(owner_id);

CREATE INDEX items_category_id_idx
    ON items(category_id);

CREATE INDEX items_status_idx
    ON items(status);

CREATE INDEX items_deleted_at_idx
    ON items(deleted_at);

CREATE TABLE photos (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    item_id    UUID        NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    photo_url  TEXT        NOT NULL,
    sort_order INT         NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX photos_item_id_idx
    ON photos(item_id);

CREATE INDEX photos_sort_order_idx
    ON photos(item_id, sort_order);

CREATE TABLE availability (
    item_id        UUID    NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    available_date DATE    NOT NULL,
    is_available   BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (item_id, available_date)
);

CREATE INDEX availability_date_idx
    ON availability(available_date);

CREATE INDEX availability_item_available_idx
    ON availability(item_id, is_available);

CREATE TABLE favorite_items (
    user_id    UUID        NOT NULL,
    item_id    UUID        NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, item_id)
);

CREATE INDEX favorite_items_user_id_idx
    ON favorite_items(user_id);

CREATE INDEX favorite_items_item_id_idx
    ON favorite_items(item_id);

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_categories_updated_at
BEFORE UPDATE ON categories
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_items_updated_at
BEFORE UPDATE ON items
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();