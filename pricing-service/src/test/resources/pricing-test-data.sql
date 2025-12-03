-- Test data initialization for pricing database (H2)

-- Create stores table
CREATE TABLE IF NOT EXISTS stores (
    store_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_code VARCHAR(50) UNIQUE NOT NULL,
    store_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create pricing_rules table
CREATE TABLE IF NOT EXISTS pricing_rules (
    rule_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id INT NOT NULL,
    store_id BIGINT,
    price DECIMAL(10,2) NOT NULL,
    promotion VARCHAR(50) NOT NULL DEFAULT 'NONE',
    promotion_value DECIMAL(10,2),
    is_global BOOLEAN NOT NULL DEFAULT FALSE,
    valid_from TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valid_to TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (store_id) REFERENCES stores(store_id)
);

-- Insert test stores
INSERT INTO stores (store_id, store_code, store_name) VALUES
(1, 'LON001', 'London Central'),
(2, 'MAN001', 'Manchester Store'),
(3, 'BIR001', 'Birmingham Store');

-- Insert test pricing rules
-- Global rules (network-wide pricing)
INSERT INTO pricing_rules (item_id, price, promotion, promotion_value, is_global, created_by) VALUES
(1, 79.99, 'NONE', NULL, TRUE, 'System'),
(2, 12.99, 'THREE_FOR_TWO', NULL, TRUE, 'System'),
(3, 8.99, 'BOGOF', NULL, TRUE, 'System'),
(4, 24.99, 'PERCENTAGE_OFF', 10, TRUE, 'System'),
(5, 149.99, 'FIXED_DISCOUNT', 5, TRUE, 'System');

-- Store-specific rules (override global for specific stores)
INSERT INTO pricing_rules (item_id, store_id, price, promotion, promotion_value, is_global, created_by) VALUES
(10, 1, 84.99, 'PERCENTAGE_OFF', 15, FALSE, 'London Manager'),
(10, NULL, 89.99, 'NONE', NULL, TRUE, 'System'); -- Global rule for item 10
