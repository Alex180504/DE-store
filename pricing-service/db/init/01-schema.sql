-- Pricing Database Schema (PostgreSQL)
-- Supports hierarchical pricing rules: Global + Store-level overrides

-- Extension for UUID support
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Stores table: Network stores that can have specific pricing
CREATE TABLE IF NOT EXISTS stores (
    store_id BIGSERIAL PRIMARY KEY,
    store_code VARCHAR(20) UNIQUE NOT NULL,
    store_name VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Pricing rules table: Hierarchical pricing with global/store-level support
CREATE TABLE IF NOT EXISTS pricing_rules (
    rule_id BIGSERIAL PRIMARY KEY,
    item_id INT NOT NULL,                          -- Reference to warehouse.items.item_id
    store_id BIGINT REFERENCES stores(store_id),   -- NULL = Global rule, NOT NULL = Store-specific
    
    -- Pricing information
    price DECIMAL(10,2) NOT NULL,                  -- Override price or base price
    promotion VARCHAR(50) DEFAULT 'NONE',          -- Promotion type (NONE, THREE_FOR_TWO, BOGOF, etc.)
    promotion_value DECIMAL(10,2),                 -- For PERCENTAGE_OFF or FIXED_DISCOUNT
    
    -- Rule scope and validity
    is_global BOOLEAN DEFAULT FALSE,               -- TRUE = Global (network-wide), FALSE = Store-specific
    valid_from TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valid_to TIMESTAMP,                            -- NULL = no expiration
    
    -- Metadata
    is_active BOOLEAN DEFAULT TRUE,
    created_by VARCHAR(100),                       -- Manager who created the rule
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT valid_price CHECK (price >= 0),
    CONSTRAINT valid_promotion_value CHECK (promotion_value IS NULL OR promotion_value >= 0),
    CONSTRAINT valid_date_range CHECK (valid_from < valid_to OR valid_to IS NULL),
    
    -- Indexes for performance
    CONSTRAINT unique_item_store_active UNIQUE (item_id, store_id, valid_from) 
);

-- Indexes for query optimization
CREATE INDEX idx_pricing_item ON pricing_rules(item_id);
CREATE INDEX idx_pricing_store ON pricing_rules(store_id);
CREATE INDEX idx_pricing_active ON pricing_rules(is_active, valid_from, valid_to);
CREATE INDEX idx_pricing_item_store ON pricing_rules(item_id, store_id);

-- Audit trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_pricing_rules_updated_at BEFORE UPDATE
    ON pricing_rules FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_stores_updated_at BEFORE UPDATE
    ON stores FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Sample stores data
INSERT INTO stores (store_code, store_name, location) VALUES
('HQ-GLOBAL', 'Global Pricing (Network-wide)', 'Headquarters'),
('LON-001', 'London Central', 'London, UK'),
('MAN-001', 'Manchester Store', 'Manchester, UK'),
('BIR-001', 'Birmingham Store', 'Birmingham, UK'),
('GLA-001', 'Glasgow Store', 'Glasgow, UK');

-- Sample global pricing rules (store_id = NULL, is_global = TRUE)
-- Global rules with promotions
INSERT INTO pricing_rules (item_id, store_id, price, promotion, promotion_value, is_global, created_by) VALUES
-- Power tools: Global pricing with occasional promotions
(1, NULL, 79.99, 'PERCENTAGE_OFF', 10.00, TRUE, 'network_manager'),  -- Cordless Drill: 10% off
(2, NULL, 12.99, 'NONE', NULL, TRUE, 'network_manager'),              -- Hammer
(3, NULL, 22.99, 'NONE', NULL, TRUE, 'network_manager'),              -- Screwdriver Set
(4, NULL, 129.99, 'NONE', NULL, TRUE, 'network_manager'),             -- Circular Saw

-- Paint products: 3 for 2 promotion
(9, NULL, 34.99, 'THREE_FOR_TWO', NULL, TRUE, 'network_manager'),     -- Emulsion Paint
(10, NULL, 9.99, 'THREE_FOR_TWO', NULL, TRUE, 'network_manager'),     -- Paint Roller
(11, NULL, 14.99, 'THREE_FOR_TWO', NULL, TRUE, 'network_manager'),    -- Paintbrush Set

-- Building materials: Free delivery for bulk orders
(15, NULL, 6.99, 'FREE_DELIVERY', NULL, TRUE, 'network_manager'),     -- Cement
(16, NULL, 4.99, 'FREE_DELIVERY', NULL, TRUE, 'network_manager'),     -- Sand

-- Garden equipment
(27, NULL, 139.99, 'FIXED_DISCOUNT', 20.00, TRUE, 'network_manager'); -- Lawn Mower: £20 off

-- Store-specific pricing overrides (overrides global pricing)
-- London store: Premium pricing
INSERT INTO pricing_rules (item_id, store_id, price, promotion, promotion_value, is_global, created_by) VALUES
(1, 2, 84.99, 'NONE', NULL, FALSE, 'store_manager_lon'),              -- Cordless Drill: higher price in London
(27, 2, 159.99, 'NONE', NULL, FALSE, 'store_manager_lon');            -- Lawn Mower: higher price

-- Manchester store: Special BOGOF promotion
INSERT INTO pricing_rules (item_id, store_id, price, promotion, promotion_value, is_global, created_by) VALUES
(2, 3, 12.99, 'BOGOF', NULL, FALSE, 'store_manager_man'),             -- Hammer: BOGOF
(5, 3, 8.99, 'BOGOF', NULL, FALSE, 'store_manager_man');              -- Tape Measure: BOGOF

-- View for active pricing rules
CREATE OR REPLACE VIEW active_pricing_rules AS
SELECT 
    pr.rule_id,
    pr.item_id,
    pr.store_id,
    s.store_code,
    s.store_name,
    pr.price,
    pr.promotion,
    pr.promotion_value,
    pr.is_global,
    pr.valid_from,
    pr.valid_to,
    pr.created_by
FROM pricing_rules pr
LEFT JOIN stores s ON pr.store_id = s.store_id
WHERE pr.is_active = TRUE
  AND pr.valid_from <= CURRENT_TIMESTAMP
  AND (pr.valid_to IS NULL OR pr.valid_to > CURRENT_TIMESTAMP)
ORDER BY pr.item_id, pr.is_global DESC, pr.store_id NULLS FIRST;

-- View for pricing conflicts (multiple active rules for same item/store)
CREATE OR REPLACE VIEW pricing_conflicts AS
SELECT 
    item_id,
    store_id,
    COUNT(*) as rule_count
FROM pricing_rules
WHERE is_active = TRUE
  AND valid_from <= CURRENT_TIMESTAMP
  AND (valid_to IS NULL OR valid_to > CURRENT_TIMESTAMP)
GROUP BY item_id, store_id
HAVING COUNT(*) > 1;

-- Comments for documentation
COMMENT ON TABLE pricing_rules IS 'Hierarchical pricing rules supporting global (is_global=TRUE, store_id=NULL) and store-specific (is_global=FALSE, store_id NOT NULL) pricing';
COMMENT ON COLUMN pricing_rules.is_global IS 'TRUE = Global network-wide pricing, FALSE = Store-specific pricing (overrides global)';
COMMENT ON COLUMN pricing_rules.promotion IS 'Type of promotion applied: NONE, THREE_FOR_TWO, BOGOF, FREE_DELIVERY, PERCENTAGE_OFF, FIXED_DISCOUNT';
COMMENT ON COLUMN pricing_rules.promotion_value IS 'Value for PERCENTAGE_OFF (e.g., 10.00 for 10%) or FIXED_DISCOUNT (e.g., 5.00 for £5 off)';
