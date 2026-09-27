-- ============================================================================
-- Initial Data Seed Script
-- ----------------------------------------------------------------------------
-- SECURITY: This script inserts sample product data only.
-- No users or credentials are auto-created — admin user must be created
-- manually or via a separate, secured seeding mechanism.
-- ============================================================================

-- Products (sample data)
INSERT INTO products (name, description, price, quantity) VALUES
    ('Dell Inspiron Notebook', 'Notebook Dell Inspiron 15, 16GB RAM, 512GB SSD, Intel Core i7', 4500.00, 10),
    ('Logitech MX Master 3 Mouse', 'Wireless ergonomic mouse with precise scroll and multiple connections', 350.00, 50),
    ('Redragon Mechanical Keyboard', 'Mechanical keyboard with Blue switches and customizable RGB lighting', 280.00, 30),
    ('Samsung 24" Monitor', '24-inch LED Full HD monitor, 75Hz, IPS panel', 899.99, 25),
    ('HyperX Gaming Headset', 'Gaming headset with 7.1 surround sound and detachable microphone', 399.99, 40)
ON CONFLICT DO NOTHING;
