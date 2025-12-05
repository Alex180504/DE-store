-- Loyalty Service Database Schema (PostgreSQL)
-- Manages loyalty points rules, customer balances, and redemption offers

-- Extension for UUID support
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

/**
 * Product Points Rules table: Defines points earned per product
 * <p>
 * Network managers configure how many loyalty points customers earn
 * when purchasing specific products. Rules have validity periods and
 * are archived rather than deleted for historical calculation accuracy.
 * </p>
 */
CREATE TABLE IF NOT EXISTS product_points_rules (
    rule_id SERIAL PRIMARY KEY,
    item_id INT NOT NULL,  -- References warehouse.items.item_id
    
    -- Points configuration
    points_per_unit INT NOT NULL CHECK (points_per_unit >= 0),
    points_per_gbp DECIMAL(10,2) CHECK (points_per_gbp >= 0),  -- Alternative: points per £1 spent
    
    -- Rule metadata
    rule_name VARCHAR(100) NOT NULL,
    description TEXT,
    
    -- Validity period
    valid_from TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valid_to TIMESTAMP,  -- NULL = no expiration
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Audit fields
    created_by VARCHAR(100) NOT NULL,  -- Username of network manager
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deactivated_at TIMESTAMP,
    
    -- Constraints
    CONSTRAINT valid_product_points_date_range CHECK (valid_from < valid_to OR valid_to IS NULL),
    CONSTRAINT valid_points_config CHECK (points_per_unit > 0 OR points_per_gbp > 0)
);

CREATE INDEX idx_product_points_item ON product_points_rules(item_id);
CREATE INDEX idx_product_points_active ON product_points_rules(is_active, valid_from, valid_to);
CREATE INDEX idx_product_points_validity ON product_points_rules(valid_from, valid_to);

/**
 * Bonus Offers table: Additional points for spending thresholds
 * <p>
 * Defines bonus points awarded when customers spend over certain amounts.
 * Examples: "50 bonus points for purchases over £100"
 * Can be global or store-specific.
 * </p>
 */
CREATE TABLE IF NOT EXISTS bonus_offers (
    offer_id SERIAL PRIMARY KEY,
    
    -- Offer configuration
    offer_name VARCHAR(100) NOT NULL,
    description TEXT,
    minimum_spend DECIMAL(10,2) NOT NULL CHECK (minimum_spend > 0),
    bonus_points INT NOT NULL CHECK (bonus_points > 0),
    
    -- Scope
    store_id INT,  -- NULL = Global offer, NOT NULL = Store-specific
    is_global BOOLEAN DEFAULT TRUE,
    
    -- Validity period
    valid_from TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valid_to TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Audit fields
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deactivated_at TIMESTAMP,
    
    -- Constraints
    CONSTRAINT valid_bonus_date_range CHECK (valid_from < valid_to OR valid_to IS NULL)
);

CREATE INDEX idx_bonus_offers_active ON bonus_offers(is_active, valid_from, valid_to);
CREATE INDEX idx_bonus_offers_store ON bonus_offers(store_id);
CREATE INDEX idx_bonus_offers_validity ON bonus_offers(valid_from, valid_to);

/**
 * Redemption Offers table: Point-based discounts on items
 * <p>
 * Network managers configure discounts that customers can apply
 * by spending loyalty points. Example: "10% off item X for 50 points"
 * Points are deducted when the offer is applied at checkout.
 * </p>
 */
CREATE TABLE IF NOT EXISTS redemption_offers (
    redemption_id SERIAL PRIMARY KEY,
    item_id INT NOT NULL,  -- References warehouse.items.item_id
    
    -- Discount configuration
    offer_name VARCHAR(100) NOT NULL,
    description TEXT,
    discount_percentage DECIMAL(5,2) NOT NULL CHECK (discount_percentage > 0 AND discount_percentage <= 100),
    points_cost INT NOT NULL CHECK (points_cost > 0),
    
    -- Usage limits
    max_uses_per_customer INT,  -- NULL = unlimited
    max_total_uses INT,  -- NULL = unlimited
    current_total_uses INT DEFAULT 0,
    
    -- Validity period
    valid_from TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valid_to TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Audit fields
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deactivated_at TIMESTAMP,
    
    -- Constraints
    CONSTRAINT valid_redemption_date_range CHECK (valid_from < valid_to OR valid_to IS NULL),
    CONSTRAINT valid_usage_tracking CHECK (current_total_uses <= max_total_uses OR max_total_uses IS NULL)
);

CREATE INDEX idx_redemption_offers_item ON redemption_offers(item_id);
CREATE INDEX idx_redemption_offers_active ON redemption_offers(is_active, valid_from, valid_to);
CREATE INDEX idx_redemption_offers_validity ON redemption_offers(valid_from, valid_to);

/**
 * Customer Points Balance table: Current loyalty points per customer
 * <p>
 * Tracks each customer's current loyalty points balance.
 * Includes versioning for optimistic locking during concurrent redemptions.
 * Last update timestamp enables incremental calculation from accounting DB.
 * </p>
 */
CREATE TABLE IF NOT EXISTS customer_points_balance (
    balance_id SERIAL PRIMARY KEY,
    customer_id INT UNIQUE NOT NULL,  -- References accounting.customers.customer_id
    
    -- Points tracking
    current_balance INT NOT NULL DEFAULT 0 CHECK (current_balance >= 0),
    lifetime_earned INT NOT NULL DEFAULT 0 CHECK (lifetime_earned >= 0),
    lifetime_redeemed INT NOT NULL DEFAULT 0 CHECK (lifetime_redeemed >= 0),
    
    -- Optimistic locking for concurrent redemptions
    version INT NOT NULL DEFAULT 1,
    
    -- Update tracking for incremental calculation
    last_calculated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_transaction_processed_id INT,  -- Last accounting.transactions.transaction_id processed
    
    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT valid_balance_calculation CHECK (current_balance = lifetime_earned - lifetime_redeemed)
);

CREATE INDEX idx_customer_balance_customer ON customer_points_balance(customer_id);
CREATE INDEX idx_customer_balance_last_calc ON customer_points_balance(last_calculated_at);

/**
 * Customer Contact Info table: Contact details for finance requests
 * <p>
 * Stores customer contact information needed for finance approval requests.
 * Updated when customer makes their first finance request.
 * </p>
 */
CREATE TABLE IF NOT EXISTS customer_contact_info (
    contact_id SERIAL PRIMARY KEY,
    customer_id INT UNIQUE NOT NULL,  -- References accounting.customers.customer_id
    
    -- Contact details
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(255),
    
    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_customer_contact_customer ON customer_contact_info(customer_id);

/**
 * Points Transactions table: Audit trail of all points earned/redeemed
 * <p>
 * Complete history of every points transaction.
 * Links to accounting transactions for earned points.
 * Links to basket/checkout for redeemed points.
 * Enables point recalculation and customer service disputes.
 * </p>
 */
CREATE TABLE IF NOT EXISTS points_transactions (
    points_transaction_id SERIAL PRIMARY KEY,
    customer_id INT NOT NULL,
    
    -- Transaction details
    transaction_type VARCHAR(20) NOT NULL CHECK (transaction_type IN ('EARNED', 'REDEEMED', 'BONUS', 'ADJUSTMENT', 'EXPIRED', 'REVERSED')),
    points_amount INT NOT NULL,  -- Positive for earned, negative for redeemed
    
    -- Balance tracking (nullable for incremental transactions)
    balance_before INT,
    balance_after INT,
    
    -- Source tracking
    accounting_transaction_id INT,  -- For EARNED: references accounting.transactions.transaction_id
    redemption_offer_id INT,  -- For REDEEMED: references redemption_offers.redemption_id
    basket_reference VARCHAR(100),  -- External shopping system reference
    
    -- Rule tracking (for earned points)
    product_rule_id INT,  -- References product_points_rules.rule_id
    bonus_offer_id INT,  -- References bonus_offers.offer_id
    
    -- Description and metadata
    description TEXT,
    transaction_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT valid_points_balance CHECK (
        (balance_before IS NULL AND balance_after IS NULL) OR
        (balance_after = balance_before + points_amount)
    ),
    CONSTRAINT valid_earned_reference CHECK (
        (transaction_type = 'EARNED' AND accounting_transaction_id IS NOT NULL) OR
        (transaction_type != 'EARNED')
    ),
    CONSTRAINT valid_redeemed_reference CHECK (
        (transaction_type = 'REDEEMED' AND redemption_offer_id IS NOT NULL) OR
        (transaction_type != 'REDEEMED')
    )
);

CREATE INDEX idx_points_trans_customer ON points_transactions(customer_id);
CREATE INDEX idx_points_trans_type ON points_transactions(transaction_type);
CREATE INDEX idx_points_trans_date ON points_transactions(transaction_date);
CREATE INDEX idx_points_trans_acct_ref ON points_transactions(accounting_transaction_id);
CREATE INDEX idx_points_trans_basket ON points_transactions(basket_reference);

/**
 * Redemption Usage Tracking table: Tracks customer usage of redemption offers
 * <p>
 * Prevents customers from exceeding per-customer usage limits.
 * Enables enforcement of max_uses_per_customer on redemption offers.
 * </p>
 */
CREATE TABLE IF NOT EXISTS redemption_usage (
    usage_id SERIAL PRIMARY KEY,
    redemption_offer_id INT NOT NULL REFERENCES redemption_offers(redemption_id),
    customer_id INT NOT NULL,
    
    -- Usage tracking
    usage_count INT NOT NULL DEFAULT 1,
    first_used_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraint for uniqueness
    CONSTRAINT unique_redemption_customer UNIQUE (redemption_offer_id, customer_id)
);

CREATE INDEX idx_redemption_usage_offer ON redemption_usage(redemption_offer_id);
CREATE INDEX idx_redemption_usage_customer ON redemption_usage(customer_id);

-- Auto-update timestamp triggers
CREATE OR REPLACE FUNCTION update_loyalty_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER product_points_rules_updated_at_trigger
BEFORE UPDATE ON product_points_rules
FOR EACH ROW
EXECUTE FUNCTION update_loyalty_updated_at();

CREATE TRIGGER bonus_offers_updated_at_trigger
BEFORE UPDATE ON bonus_offers
FOR EACH ROW
EXECUTE FUNCTION update_loyalty_updated_at();

CREATE TRIGGER redemption_offers_updated_at_trigger
BEFORE UPDATE ON redemption_offers
FOR EACH ROW
EXECUTE FUNCTION update_loyalty_updated_at();

CREATE TRIGGER customer_points_balance_updated_at_trigger
BEFORE UPDATE ON customer_points_balance
FOR EACH ROW
EXECUTE FUNCTION update_loyalty_updated_at();

/**
 * View: Active Product Points Rules
 * Shows currently active rules for points calculation
 */
CREATE OR REPLACE VIEW active_product_points AS
SELECT 
    rule_id,
    item_id,
    points_per_unit,
    points_per_gbp,
    rule_name,
    valid_from,
    valid_to
FROM product_points_rules
WHERE is_active = TRUE
    AND valid_from <= CURRENT_TIMESTAMP
    AND (valid_to IS NULL OR valid_to > CURRENT_TIMESTAMP);

/**
 * View: Active Bonus Offers
 * Shows currently active bonus offers
 */
CREATE OR REPLACE VIEW active_bonus_offers AS
SELECT 
    offer_id,
    offer_name,
    minimum_spend,
    bonus_points,
    store_id,
    is_global,
    valid_from,
    valid_to
FROM bonus_offers
WHERE is_active = TRUE
    AND valid_from <= CURRENT_TIMESTAMP
    AND (valid_to IS NULL OR valid_to > CURRENT_TIMESTAMP);

/**
 * View: Active Redemption Offers
 * Shows currently active redemption offers with usage statistics
 */
CREATE OR REPLACE VIEW active_redemption_offers AS
SELECT 
    r.redemption_id,
    r.item_id,
    r.offer_name,
    r.description,
    r.discount_percentage,
    r.points_cost,
    r.max_uses_per_customer,
    r.max_total_uses,
    r.current_total_uses,
    (r.max_total_uses IS NULL OR r.current_total_uses < r.max_total_uses) as available,
    r.valid_from,
    r.valid_to
FROM redemption_offers r
WHERE r.is_active = TRUE
    AND r.valid_from <= CURRENT_TIMESTAMP
    AND (r.valid_to IS NULL OR r.valid_to > CURRENT_TIMESTAMP)
    AND (r.max_total_uses IS NULL OR r.current_total_uses < r.max_total_uses);

/**
 * View: Customer Points Summary
 * Comprehensive view of customer loyalty status
 */
CREATE OR REPLACE VIEW customer_points_summary AS
SELECT 
    cpb.customer_id,
    cpb.current_balance,
    cpb.lifetime_earned,
    cpb.lifetime_redeemed,
    cpb.last_calculated_at,
    COUNT(DISTINCT pt.points_transaction_id) as total_transactions,
    SUM(CASE WHEN pt.transaction_type = 'EARNED' THEN pt.points_amount ELSE 0 END) as total_earned_via_transactions,
    SUM(CASE WHEN pt.transaction_type = 'REDEEMED' THEN ABS(pt.points_amount) ELSE 0 END) as total_redeemed_via_transactions
FROM customer_points_balance cpb
LEFT JOIN points_transactions pt ON cpb.customer_id = pt.customer_id
GROUP BY cpb.customer_id, cpb.current_balance, cpb.lifetime_earned, 
         cpb.lifetime_redeemed, cpb.last_calculated_at;

