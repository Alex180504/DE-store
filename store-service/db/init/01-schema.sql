-- Store Database Schema (PostgreSQL)
-- Manages store locations and information for DE-Store network

-- Stores table: Store locations and details
CREATE TABLE IF NOT EXISTS stores (
    store_id SERIAL PRIMARY KEY,
    store_code VARCHAR(20) UNIQUE NOT NULL,
    store_name VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    postcode VARCHAR(10) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT store_code_format CHECK (store_code ~ '^[A-Z]{3}-[0-9]{3}$')
);

-- Indexes for performance
CREATE INDEX idx_stores_code ON stores(store_code);
CREATE INDEX idx_stores_active ON stores(is_active);
CREATE INDEX idx_stores_postcode ON stores(postcode);

-- Auto-update timestamp trigger
CREATE OR REPLACE FUNCTION update_stores_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER stores_updated_at_trigger
BEFORE UPDATE ON stores
FOR EACH ROW
EXECUTE FUNCTION update_stores_updated_at();

-- Insert initial store data
INSERT INTO stores (store_code, store_name, address, postcode, is_active) VALUES
('LON-001', 'London Central', '123 Oxford Street, London', 'W1D 1BS', TRUE),
('MAN-001', 'Manchester Store', '45 Market Street, Manchester', 'M1 1WR', TRUE),
('BIR-001', 'Birmingham Store', '78 Bull Street, Birmingham', 'B4 6AF', TRUE),
('GLA-001', 'Glasgow Store', '92 Buchanan Street, Glasgow', 'G1 3BA', TRUE),
('EDI-001', 'Edinburgh Store', '34 Princes Street, Edinburgh', 'EH2 2BY', TRUE);

-- Verify data
SELECT store_id, store_code, store_name, is_active FROM stores ORDER BY store_id;
