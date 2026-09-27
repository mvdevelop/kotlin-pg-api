-- ============================================================================
-- Flyway Migration V1 — Create Products table
-- ----------------------------------------------------------------------------
-- SECURITY NOTES:
-- - CHECK constraints enforce data integrity at DB level (defense in depth)
-- - UNIQUE constraint on name prevents duplicates
-- - Indexes on name and price for query performance
-- - Timestamps are auto-generated (not user-controlled)
-- ============================================================================

CREATE TABLE products (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL,
    description TEXT          NOT NULL,
    price       NUMERIC(10,2) NOT NULL CHECK (price > 0),
    quantity    INTEGER       NOT NULL CHECK (quantity >= 0),
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_products_name UNIQUE (name)
);

CREATE INDEX idx_products_name  ON products(name);
CREATE INDEX idx_products_price ON products(price);

-- Seed initial data
INSERT INTO products (name, description, price, quantity) VALUES
    ('Dell Inspiron Notebook', 'Notebook Dell Inspiron 15, 16GB RAM, 512GB SSD, Intel Core i7', 4500.00, 10),
    ('Logitech MX Master 3 Mouse', 'Wireless ergonomic mouse with precise scroll and multiple connections', 350.00, 50),
    ('Redragon Mechanical Keyboard', 'Mechanical keyboard with Blue switches and customizable RGB lighting', 280.00, 30);
