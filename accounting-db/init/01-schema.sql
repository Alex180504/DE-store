-- Accounting Database Schema
-- Stores transaction records for the DE-Store system
-- This database is READ-ONLY for loyalty-service

-- ============================================================
-- TRANSACTIONS TABLE
-- ============================================================

/**
 * Transactions table: Records all customer purchase transactions
 * <p>
 * Used by loyalty-service to calculate points earned from purchases.
 * The loyalty service queries completed transactions to determine
 * points based on items purchased and total amounts spent.
 * </p>
 */
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id BIGSERIAL PRIMARY KEY,
    customer_id INT NOT NULL,
    store_id INT NOT NULL,
    transaction_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2) NOT NULL CHECK (total_amount >= 0),
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    payment_method VARCHAR(50),
    
    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT valid_status CHECK (status IN ('PENDING', 'COMPLETED', 'CANCELLED', 'REFUNDED'))
);

-- Indexes for loyalty service queries
CREATE INDEX idx_transactions_customer ON transactions(customer_id);
CREATE INDEX idx_transactions_store ON transactions(store_id);
CREATE INDEX idx_transactions_date ON transactions(transaction_date);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_customer_date ON transactions(customer_id, transaction_date);

-- ============================================================
-- TRANSACTION ITEMS TABLE
-- ============================================================

/**
 * Transaction Items table: Line items for each transaction
 * <p>
 * Records individual items purchased in each transaction.
 * Used by loyalty service to calculate points per product.
 * </p>
 */
CREATE TABLE IF NOT EXISTS transaction_items (
    transaction_item_id SERIAL PRIMARY KEY,
    transaction_id BIGINT NOT NULL REFERENCES transactions(transaction_id) ON DELETE CASCADE,
    item_id INT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10,2) NOT NULL CHECK (unit_price >= 0),
    line_total DECIMAL(10,2) NOT NULL CHECK (line_total >= 0),
    
    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for loyalty service queries
CREATE INDEX idx_transaction_items_transaction ON transaction_items(transaction_id);
CREATE INDEX idx_transaction_items_item ON transaction_items(item_id);

-- ============================================================
-- COMMENTS
-- ============================================================

COMMENT ON TABLE transactions IS 'Customer purchase transactions - read by loyalty service for points calculation';
COMMENT ON TABLE transaction_items IS 'Line items for each transaction - read by loyalty service for product-based points';

COMMENT ON COLUMN transactions.customer_id IS 'References customers.customer_id from store database';
COMMENT ON COLUMN transactions.store_id IS 'References stores.store_id from store database';
COMMENT ON COLUMN transactions.status IS 'Transaction status: PENDING, COMPLETED, CANCELLED, REFUNDED';
COMMENT ON COLUMN transaction_items.item_id IS 'References items.item_id from warehouse database';
