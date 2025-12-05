-- Accounting Database Schema (PostgreSQL)
-- Tracks customer purchases, loyalty cards, and finance approval for DE-Store network

-- Extension for UUID support
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

/**
 * Customers table: Core customer information
 * <p>
 * Stores customer demographic and contact information.
 * Links to loyalty cards and transaction history.
 * Supports finance approval eligibility tracking.
 * </p>
 */
CREATE TABLE IF NOT EXISTS customers (
    customer_id SERIAL PRIMARY KEY,
    email VARCHAR(100) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    address VARCHAR(255),
    postcode VARCHAR(10),
    
    -- Account status
    is_active BOOLEAN DEFAULT TRUE,
    email_verified BOOLEAN DEFAULT FALSE,
    
    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_purchase_at TIMESTAMP,
    
    -- Indexes
    CONSTRAINT customer_email_format CHECK (email ~ '^[^@]+@[^@]+\.[^@]+$')
);

CREATE INDEX idx_customers_email ON customers(email);
CREATE INDEX idx_customers_active ON customers(is_active);
CREATE INDEX idx_customers_postcode ON customers(postcode);

/**
 * Loyalty Cards table: Customer loyalty program membership
 * <p>
 * Tracks loyalty card status, points, and tier levels.
 * Used for eligibility checks in finance approval.
 * Points are accumulated based on purchase amounts.
 * </p>
 */
CREATE TABLE IF NOT EXISTS loyalty_cards (
    card_id SERIAL PRIMARY KEY,
    customer_id INT NOT NULL REFERENCES customers(customer_id) ON DELETE CASCADE,
    card_number VARCHAR(20) UNIQUE NOT NULL,
    
    -- Loyalty status
    tier VARCHAR(20) NOT NULL DEFAULT 'BRONZE' CHECK (tier IN ('BRONZE', 'SILVER', 'GOLD', 'PLATINUM')),
    points_balance INT DEFAULT 0 CHECK (points_balance >= 0),
    lifetime_points INT DEFAULT 0 CHECK (lifetime_points >= 0),
    
    -- Card validity
    issue_date DATE NOT NULL DEFAULT CURRENT_DATE,
    expiry_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT card_number_format CHECK (card_number ~ '^LC-[0-9]{10}$'),
    CONSTRAINT valid_expiry CHECK (expiry_date > issue_date)
);

CREATE INDEX idx_loyalty_customer ON loyalty_cards(customer_id);
CREATE INDEX idx_loyalty_card_number ON loyalty_cards(card_number);
CREATE INDEX idx_loyalty_tier ON loyalty_cards(tier);
CREATE INDEX idx_loyalty_active ON loyalty_cards(is_active);

/**
 * Transactions table: Purchase transaction records
 * <p>
 * Stores header-level transaction information.
 * Links to transaction_items for line-item details.
 * Tracks payment method and finance approval status.
 * </p>
 */
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id SERIAL PRIMARY KEY,
    transaction_uuid UUID DEFAULT uuid_generate_v4() UNIQUE NOT NULL,
    customer_id INT NOT NULL REFERENCES customers(customer_id),
    store_id INT NOT NULL,  -- References store-service.stores.store_id
    
    -- Transaction totals
    subtotal DECIMAL(10,2) NOT NULL CHECK (subtotal >= 0),
    promotion_discount DECIMAL(10,2) DEFAULT 0.00 CHECK (promotion_discount >= 0),
    loyalty_discount DECIMAL(10,2) DEFAULT 0.00 CHECK (loyalty_discount >= 0),
    total_amount DECIMAL(10,2) NOT NULL CHECK (total_amount >= 0),
    
    -- Payment information
    payment_method VARCHAR(50) NOT NULL CHECK (payment_method IN ('CASH', 'CARD', 'FINANCE', 'LOYALTY_POINTS')),
    finance_approved BOOLEAN DEFAULT FALSE,
    finance_approval_ref VARCHAR(50),
    
    -- Loyalty points
    points_earned INT DEFAULT 0,
    points_redeemed INT DEFAULT 0,
    
    -- Transaction metadata
    transaction_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT valid_total CHECK (total_amount = subtotal - promotion_discount - loyalty_discount),
    CONSTRAINT finance_ref_required CHECK (
        (payment_method = 'FINANCE' AND finance_approval_ref IS NOT NULL) OR
        (payment_method != 'FINANCE')
    )
);

CREATE INDEX idx_transactions_customer ON transactions(customer_id);
CREATE INDEX idx_transactions_store ON transactions(store_id);
CREATE INDEX idx_transactions_date ON transactions(transaction_date);
CREATE INDEX idx_transactions_payment ON transactions(payment_method);
CREATE INDEX idx_transactions_uuid ON transactions(transaction_uuid);

/**
 * Transaction Items table: Line items for each transaction
 * <p>
 * Stores individual items purchased in each transaction.
 * Tracks both original price and final price after promotions.
 * Links to warehouse items and pricing rules.
 * </p>
 */
CREATE TABLE IF NOT EXISTS transaction_items (
    item_line_id SERIAL PRIMARY KEY,
    transaction_id INT NOT NULL REFERENCES transactions(transaction_id) ON DELETE CASCADE,
    item_id INT NOT NULL,  -- References warehouse.items.item_id
    
    -- Quantity and pricing
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price_original DECIMAL(10,2) NOT NULL CHECK (unit_price_original >= 0),
    unit_price_final DECIMAL(10,2) NOT NULL CHECK (unit_price_final >= 0),
    promotion_applied VARCHAR(50) DEFAULT 'NONE',
    
    -- Line totals
    line_total_original DECIMAL(10,2) NOT NULL,
    line_total_final DECIMAL(10,2) NOT NULL,
    line_discount DECIMAL(10,2) DEFAULT 0.00,
    
    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT valid_line_totals CHECK (
        line_total_original = quantity * unit_price_original AND
        line_total_final <= line_total_original AND
        line_discount = line_total_original - line_total_final
    )
);

CREATE INDEX idx_transaction_items_transaction ON transaction_items(transaction_id);
CREATE INDEX idx_transaction_items_item ON transaction_items(item_id);

/**
 * Finance Approvals table: Credit/finance application records
 * <p>
 * Tracks customer finance applications for large purchases.
 * Supports approval workflow and credit limit management.
 * Used for payment method validation.
 * </p>
 */
CREATE TABLE IF NOT EXISTS finance_approvals (
    approval_id SERIAL PRIMARY KEY,
    customer_id INT NOT NULL REFERENCES customers(customer_id),
    
    -- Application details
    application_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    requested_amount DECIMAL(10,2) NOT NULL CHECK (requested_amount > 0),
    approved_amount DECIMAL(10,2) CHECK (approved_amount >= 0),
    
    -- Approval status
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'EXPIRED')),
    approval_date TIMESTAMP,
    expiry_date DATE,
    
    -- Credit information
    credit_score INT CHECK (credit_score BETWEEN 0 AND 999),
    monthly_income DECIMAL(10,2),
    employment_status VARCHAR(50),
    
    -- Usage tracking
    amount_used DECIMAL(10,2) DEFAULT 0.00 CHECK (amount_used >= 0),
    amount_remaining DECIMAL(10,2),
    
    -- Metadata
    approved_by VARCHAR(100),
    rejection_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT valid_approval_amount CHECK (
        (status = 'APPROVED' AND approved_amount IS NOT NULL AND approved_amount > 0) OR
        (status != 'APPROVED')
    ),
    CONSTRAINT valid_remaining CHECK (
        amount_remaining IS NULL OR amount_remaining = approved_amount - amount_used
    )
);

CREATE INDEX idx_finance_customer ON finance_approvals(customer_id);
CREATE INDEX idx_finance_status ON finance_approvals(status);
CREATE INDEX idx_finance_dates ON finance_approvals(application_date, expiry_date);

/**
 * Loyalty Points History table: Audit trail for points transactions
 * <p>
 * Tracks all points earned and redeemed.
 * Provides audit trail for customer service.
 * Links to transactions for reconciliation.
 * </p>
 */
CREATE TABLE IF NOT EXISTS loyalty_points_history (
    history_id SERIAL PRIMARY KEY,
    card_id INT NOT NULL REFERENCES loyalty_cards(card_id) ON DELETE CASCADE,
    transaction_id INT REFERENCES transactions(transaction_id),
    
    -- Points change
    points_change INT NOT NULL,  -- Positive for earn, negative for redeem
    points_type VARCHAR(20) NOT NULL CHECK (points_type IN ('EARNED', 'REDEEMED', 'EXPIRED', 'BONUS', 'ADJUSTMENT')),
    
    -- Balance snapshot
    balance_before INT NOT NULL,
    balance_after INT NOT NULL,
    
    -- Metadata
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT valid_balance_change CHECK (balance_after = balance_before + points_change)
);

CREATE INDEX idx_points_history_card ON loyalty_points_history(card_id);
CREATE INDEX idx_points_history_transaction ON loyalty_points_history(transaction_id);
CREATE INDEX idx_points_history_date ON loyalty_points_history(created_at);

-- Auto-update timestamp triggers
CREATE OR REPLACE FUNCTION update_accounting_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER customers_updated_at_trigger
BEFORE UPDATE ON customers
FOR EACH ROW
EXECUTE FUNCTION update_accounting_updated_at();

CREATE TRIGGER loyalty_cards_updated_at_trigger
BEFORE UPDATE ON loyalty_cards
FOR EACH ROW
EXECUTE FUNCTION update_accounting_updated_at();

CREATE TRIGGER finance_approvals_updated_at_trigger
BEFORE UPDATE ON finance_approvals
FOR EACH ROW
EXECUTE FUNCTION update_accounting_updated_at();

/**
 * View: Customer Summary
 * Aggregates customer spending, loyalty status, and finance eligibility
 */
CREATE OR REPLACE VIEW customer_summary AS
SELECT 
    c.customer_id,
    c.email,
    c.full_name,
    c.is_active,
    lc.card_number,
    lc.tier as loyalty_tier,
    lc.points_balance,
    lc.lifetime_points,
    COUNT(DISTINCT t.transaction_id) as total_transactions,
    COALESCE(SUM(t.total_amount), 0) as lifetime_spending,
    MAX(t.transaction_date) as last_purchase_date,
    fa.status as finance_status,
    fa.approved_amount as finance_limit,
    fa.amount_remaining as finance_available
FROM customers c
LEFT JOIN loyalty_cards lc ON c.customer_id = lc.customer_id AND lc.is_active = TRUE
LEFT JOIN transactions t ON c.customer_id = t.customer_id
LEFT JOIN finance_approvals fa ON c.customer_id = fa.customer_id 
    AND fa.status = 'APPROVED' 
    AND (fa.expiry_date IS NULL OR fa.expiry_date > CURRENT_DATE)
GROUP BY c.customer_id, c.email, c.full_name, c.is_active, 
         lc.card_number, lc.tier, lc.points_balance, lc.lifetime_points,
         fa.status, fa.approved_amount, fa.amount_remaining;

/**
 * View: Transaction Summary
 * Detailed view of transactions with customer and store information
 */
CREATE OR REPLACE VIEW transaction_summary AS
SELECT 
    t.transaction_id,
    t.transaction_uuid,
    t.transaction_date,
    c.customer_id,
    c.full_name as customer_name,
    c.email as customer_email,
    t.store_id,
    t.subtotal,
    t.promotion_discount,
    t.loyalty_discount,
    t.total_amount,
    t.payment_method,
    t.finance_approved,
    t.points_earned,
    t.points_redeemed,
    COUNT(ti.item_line_id) as items_count,
    SUM(ti.quantity) as total_quantity
FROM transactions t
JOIN customers c ON t.customer_id = c.customer_id
LEFT JOIN transaction_items ti ON t.transaction_id = ti.transaction_id
GROUP BY t.transaction_id, t.transaction_uuid, t.transaction_date,
         c.customer_id, c.full_name, c.email, t.store_id,
         t.subtotal, t.promotion_discount, t.loyalty_discount,
         t.total_amount, t.payment_method, t.finance_approved,
         t.points_earned, t.points_redeemed;

/**
 * View: Store Performance
 * Aggregates sales by store for reporting
 */
CREATE OR REPLACE VIEW store_performance AS
SELECT 
    store_id,
    COUNT(DISTINCT transaction_id) as total_transactions,
    COUNT(DISTINCT customer_id) as unique_customers,
    SUM(total_amount) as total_revenue,
    AVG(total_amount) as avg_transaction_value,
    SUM(promotion_discount + loyalty_discount) as total_discounts,
    MIN(transaction_date) as first_transaction,
    MAX(transaction_date) as last_transaction
FROM transactions
GROUP BY store_id;

