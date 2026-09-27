-- ============================================================================
-- Flyway Migration V2 — Create Users, Roles, and Audit tables
-- ----------------------------------------------------------------------------
-- SECURITY NOTES:
-- - password_hash stores ONLY BCrypt hashes (never plaintext)
-- - email is unique (prevents account hijacking via email reuse)
-- - Account lockout fields for brute-force protection
-- - Audit trail for security events (OWASP ASVS V7.1)
-- - role is a string enum (ROLE_USER, ROLE_ADMIN)
-- ============================================================================

-- Users table
CREATE TABLE users (
    id                  BIGSERIAL PRIMARY KEY,
    username            VARCHAR(50)   NOT NULL UNIQUE,
    password_hash       VARCHAR(255)  NOT NULL,
    email               VARCHAR(254)  NOT NULL UNIQUE,
    failed_login_attempts INTEGER      NOT NULL DEFAULT 0,
    account_locked      BOOLEAN       NOT NULL DEFAULT FALSE,
    account_locked_until TIMESTAMP WITH TIME ZONE,
    created_at          TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email    ON users(email);

-- User roles table (RBAC)
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role    VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role)
);

-- Audit trail table
CREATE TABLE audit_log (
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
        'USER_DELETE', 'PASSWORD_CHANGE', 'ACCOUNT_LOCKOUT'
    ))
);

CREATE INDEX idx_audit_created_at  ON audit_log(created_at);
CREATE INDEX idx_audit_event_type  ON audit_log(event_type);
CREATE INDEX idx_audit_username    ON audit_log(username);

-- Trigger for auto-updating updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
