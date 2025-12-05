-- Loyalty Service Sample Data
-- Populates loyalty rules and initial customer balances

-- ============================================================
-- PRODUCT POINTS RULES: Points earned per product
-- ============================================================

INSERT INTO product_points_rules (item_id, points_per_unit, points_per_gbp, rule_name, description, valid_from, is_active, created_by) VALUES
-- Power tools: High points (5 points per unit)
(1, 5, NULL, 'Cordless Drill Points', 'Earn 5 points per cordless drill purchased', '2024-01-01 00:00:00', TRUE, 'network_manager'),
(4, 5, NULL, 'Circular Saw Points', 'Earn 5 points per circular saw purchased', '2024-01-01 00:00:00', TRUE, 'network_manager'),
(7, 4, NULL, 'Angle Grinder Points', 'Earn 4 points per angle grinder purchased', '2024-01-01 00:00:00', TRUE, 'network_manager'),

-- Hand tools: Medium points (2-3 points per unit)
(2, 2, NULL, 'Hammer Points', 'Earn 2 points per hammer purchased', '2024-01-01 00:00:00', TRUE, 'network_manager'),
(3, 3, NULL, 'Screwdriver Set Points', 'Earn 3 points per screwdriver set purchased', '2024-01-01 00:00:00', TRUE, 'network_manager'),
(5, 1, NULL, 'Tape Measure Points', 'Earn 1 point per tape measure purchased', '2024-01-01 00:00:00', TRUE, 'network_manager'),
(6, 2, NULL, 'Level Points', 'Earn 2 points per level purchased', '2024-01-01 00:00:00', TRUE, 'network_manager'),
(8, 2, NULL, 'Pliers Set Points', 'Earn 2 points per pliers set purchased', '2024-01-01 00:00:00', TRUE, 'network_manager'),

-- Paint products: Points per £1 spent (better for variable pricing)
(9, 0, 0.5, 'Paint Points', 'Earn 0.5 points per £1 spent on paint', '2024-01-01 00:00:00', TRUE, 'network_manager'),
(10, 1, NULL, 'Paint Roller Points', 'Earn 1 point per paint roller set', '2024-01-01 00:00:00', TRUE, 'network_manager'),
(11, 1, NULL, 'Paintbrush Set Points', 'Earn 1 point per paintbrush set', '2024-01-01 00:00:00', TRUE, 'network_manager'),

-- Building materials: Low points (bulk items)
(15, 0, 0.2, 'Cement Points', 'Earn 0.2 points per £1 spent on cement', '2024-01-01 00:00:00', TRUE, 'network_manager'),
(16, 0, 0.2, 'Sand Points', 'Earn 0.2 points per £1 spent on sand', '2024-01-01 00:00:00', TRUE, 'network_manager'),
(19, 1, NULL, 'Timber Points', 'Earn 1 point per timber piece', '2024-01-01 00:00:00', TRUE, 'network_manager'),
(20, 3, NULL, 'Plywood Points', 'Earn 3 points per plywood sheet', '2024-01-01 00:00:00', TRUE, 'network_manager'),

-- Garden equipment: High points (premium items)
(27, 10, NULL, 'Lawn Mower Points', 'Earn 10 points per lawn mower purchased', '2024-01-01 00:00:00', TRUE, 'network_manager'),
(28, 2, NULL, 'Spade Points', 'Earn 2 points per spade purchased', '2024-01-01 00:00:00', TRUE, 'network_manager'),
(29, 4, NULL, 'Wheelbarrow Points', 'Earn 4 points per wheelbarrow purchased', '2024-01-01 00:00:00', TRUE, 'network_manager');

-- ============================================================
-- BONUS OFFERS: Additional points for spending thresholds
-- ============================================================

INSERT INTO bonus_offers (offer_name, description, minimum_spend, bonus_points, store_id, is_global, valid_from, is_active, created_by) VALUES
-- Global bonus offers
('Spend £50 Bonus', 'Earn 20 bonus points when you spend £50 or more', 50.00, 20, NULL, TRUE, '2024-01-01 00:00:00', TRUE, 'network_manager'),
('Spend £100 Bonus', 'Earn 50 bonus points when you spend £100 or more', 100.00, 50, NULL, TRUE, '2024-01-01 00:00:00', TRUE, 'network_manager'),
('Spend £200 Bonus', 'Earn 120 bonus points when you spend £200 or more', 200.00, 120, NULL, TRUE, '2024-01-01 00:00:00', TRUE, 'network_manager'),
('Spend £500 Bonus', 'Earn 300 bonus points when you spend £500 or more', 500.00, 300, NULL, TRUE, '2024-01-01 00:00:00', TRUE, 'network_manager'),

-- Store-specific bonus offers (London - store_id 1)
('London Big Spender', 'London exclusive: 150 bonus points for £250+ purchases', 250.00, 150, 1, FALSE, '2024-01-01 00:00:00', TRUE, 'network_manager'),

-- Store-specific bonus offers (Manchester - store_id 2)
('Manchester Weekend Bonus', 'Manchester: 80 bonus points for £150+ purchases', 150.00, 80, 2, FALSE, '2024-01-01 00:00:00', TRUE, 'network_manager');

-- ============================================================
-- REDEMPTION OFFERS: Point-based discounts
-- ============================================================

INSERT INTO redemption_offers (item_id, offer_name, description, discount_percentage, points_cost, max_uses_per_customer, max_total_uses, valid_from, is_active, created_by) VALUES
-- Power tools redemption
(1, '10% Off Cordless Drill', 'Redeem 50 points for 10% off cordless drill', 10.00, 50, 3, NULL, '2024-01-01 00:00:00', TRUE, 'network_manager'),
(4, '15% Off Circular Saw', 'Redeem 100 points for 15% off circular saw', 15.00, 100, 2, NULL, '2024-01-01 00:00:00', TRUE, 'network_manager'),
(7, '10% Off Angle Grinder', 'Redeem 40 points for 10% off angle grinder', 10.00, 40, 3, NULL, '2024-01-01 00:00:00', TRUE, 'network_manager'),

-- Paint products redemption
(9, '20% Off Paint', 'Redeem 30 points for 20% off emulsion paint', 20.00, 30, 5, NULL, '2024-01-01 00:00:00', TRUE, 'network_manager'),
(10, '15% Off Paint Roller', 'Redeem 15 points for 15% off paint roller set', 15.00, 15, NULL, NULL, '2024-01-01 00:00:00', TRUE, 'network_manager'),

-- Garden equipment redemption
(27, '25% Off Lawn Mower', 'Redeem 200 points for 25% off lawn mower', 25.00, 200, 1, 50, '2024-01-01 00:00:00', TRUE, 'network_manager'),
(28, '10% Off Spade', 'Redeem 20 points for 10% off spade', 10.00, 20, NULL, NULL, '2024-01-01 00:00:00', TRUE, 'network_manager'),

-- Building materials redemption
(15, '15% Off Cement', 'Redeem 25 points for 15% off cement (bulk orders)', 15.00, 25, NULL, NULL, '2024-01-01 00:00:00', TRUE, 'network_manager'),
(20, '20% Off Plywood', 'Redeem 60 points for 20% off plywood sheets', 20.00, 60, 3, NULL, '2024-01-01 00:00:00', TRUE, 'network_manager'),

-- Hand tools redemption
(2, '10% Off Hammer', 'Redeem 15 points for 10% off hammer', 10.00, 15, NULL, NULL, '2024-01-01 00:00:00', TRUE, 'network_manager'),
(3, '15% Off Screwdriver Set', 'Redeem 25 points for 15% off screwdriver set', 15.00, 25, 4, NULL, '2024-01-01 00:00:00', TRUE, 'network_manager');

-- ============================================================
-- CUSTOMER POINTS BALANCE: Initialize for existing customers
-- Will be calculated from accounting transactions
-- ============================================================

-- Initialize balance records for all customers (from accounting DB)
-- Points will be calculated by the loyalty service based on transaction history
INSERT INTO customer_points_balance (customer_id, current_balance, lifetime_earned, lifetime_redeemed, last_calculated_at, last_transaction_processed_id) VALUES
(1, 0, 0, 0, '2024-01-01 00:00:00', 0),
(2, 0, 0, 0, '2024-01-01 00:00:00', 0),
(3, 0, 0, 0, '2024-01-01 00:00:00', 0),
(4, 0, 0, 0, '2024-01-01 00:00:00', 0),
(5, 0, 0, 0, '2024-01-01 00:00:00', 0),
(6, 0, 0, 0, '2024-01-01 00:00:00', 0),
(7, 0, 0, 0, '2024-01-01 00:00:00', 0),
(8, 0, 0, 0, '2024-01-01 00:00:00', 0),
(9, 0, 0, 0, '2024-01-01 00:00:00', 0),
(10, 0, 0, 0, '2024-01-01 00:00:00', 0),
(11, 0, 0, 0, '2024-01-01 00:00:00', 0),
(12, 0, 0, 0, '2024-01-01 00:00:00', 0),
(13, 0, 0, 0, '2024-01-01 00:00:00', 0),
(14, 0, 0, 0, '2024-01-01 00:00:00', 0),
(15, 0, 0, 0, '2024-01-01 00:00:00', 0),
(16, 0, 0, 0, '2024-01-01 00:00:00', 0),
(17, 0, 0, 0, '2024-01-01 00:00:00', 0),
(18, 0, 0, 0, '2024-01-01 00:00:00', 0),
(19, 0, 0, 0, '2024-01-01 00:00:00', 0),
(20, 0, 0, 0, '2024-01-01 00:00:00', 0);

-- Verify data integrity
SELECT 'Product Points Rules' as table_name, COUNT(*) as record_count FROM product_points_rules WHERE is_active = TRUE
UNION ALL
SELECT 'Bonus Offers', COUNT(*) FROM bonus_offers WHERE is_active = TRUE
UNION ALL
SELECT 'Redemption Offers', COUNT(*) FROM redemption_offers WHERE is_active = TRUE
UNION ALL
SELECT 'Customer Balances', COUNT(*) FROM customer_points_balance;

-- Display active offers summary
SELECT 'Active Product Points Rules' as category, COUNT(*) as count FROM active_product_points
UNION ALL
SELECT 'Active Bonus Offers', COUNT(*) FROM active_bonus_offers
UNION ALL
SELECT 'Active Redemption Offers', COUNT(*) FROM active_redemption_offers;

