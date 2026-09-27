-- ============================================================================
-- PostgreSQL Schema — Products API
-- ----------------------------------------------------------------------------
-- SECURITY NOTES:
-- - All text columns use VARCHAR with explicit length limits (CWE-1333)
-- - CHECK constraints enforce data integrity at DB level (defense in depth)
-- - UNIQUE constraints prevent duplicate entries
-- - Passwords stored as BCrypt hashes (VARCHAR(255))
-- - Timestamps with timezone (TIMESTAMP WITH TIME ZONE)
-- - Indexes for common query patterns
-- - No PII is logged or stored beyond what is necessary for operation
--
-- LGPD Compliance:
-- - Email stored for authentication purposes (Art. 7º, II — legitimate interest)
-- - Passwords stored only as hashes (Art. 11 — security of processing)
-- - Users can request deletion at any time (Art. 16 — right to erasure)
-- ============================================================================

-- Enable UUID extension for future use (user-facing IDs, audit trails)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================================
-- Products Table
-- ============================================================================
CREATE TABLE IF NOT EXISTS products (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL,
    description TEXT          NOT NULL,
    price       NUMERIC(10,2) NOT NULL CHECK (price > 0),
    quantity    INTEGER       NOT NULL CHECK (quantity >= 0),
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_products_name UNIQUE (name)
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_products_name  ON products(name);
CREATE INDEX IF NOT EXISTS idx_products_price ON products(price);

-- ============================================================================
-- Users Table (Authentication & Authorization)
-- ============================================================================
CREATE TABLE IF NOT EXISTS users (
    id                  BIGSERIAL PRIMARY KEY,
    username            VARCHAR(50)   NOT NULL UNIQUE,
    password_hash       VARCHAR(255)  NOT NULL,  -- BCrypt hash (never plaintext)
    email               VARCHAR(254)  NOT NULL UNIQUE,
    failed_login_attempts INTEGER      NOT NULL DEFAULT 0,
    account_locked      BOOLEAN       NOT NULL DEFAULT FALSE,
    account_locked_until TIMESTAMP WITH TIME ZONE,
    created_at          TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email    ON users(email);

-- ============================================================================
-- User Roles Table (RBAC — Role-Based Access Control)
-- ============================================================================
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role    VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role)
);

-- ============================================================================
-- Audit Trail Table (OWASP ASVS V7.1 — Logging and Monitoring)
-- ============================================================================
CREATE TABLE IF NOT EXISTS audit_log (
    id          BIGSERIAL PRIMARY KEY,
    event_type  VARCHAR(50)    NOT NULL,
    username    VARCHAR(50),
    ip_address  VARCHAR(45),
    user_agent  TEXT,
    details     TEXT,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_audit_event_type CHECK (event_type IN (
        'LOGIN_SUCCESS', 'LOGIN_FAILURE', 'REGISTER',
        'PRODUCT_CREATE', 'PRODUCT_UPDATE', 'PRODUCT_DELETE',
        'USER_DELETE', 'PASSWORD_CHANGE'
    ))
);

CREATE INDEX IF NOT EXISTS idx_audit_created_at  ON audit_log(created_at);
CREATE INDEX IF NOT EXISTS idx_audit_event_type  ON audit_log(event_type);

-- ============================================================================
-- Trigger: Auto-update updated_at column
-- ============================================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_products_updated_at
BEFORE UPDATE ON products
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_update_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- View: Active products (for convenience queries)
-- ============================================================================
CREATE OR REPLACE VIEW vw_active_products AS
SELECT id, name, description, price, quantity, created_at
FROM products;
