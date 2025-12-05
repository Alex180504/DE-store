-- Accounting Database Sample Data
-- Populates realistic transaction data consistent with warehouse items, stores, and pricing rules

-- ============================================================
-- CUSTOMERS: 20 sample customers across different demographics
-- ============================================================

INSERT INTO customers (customer_id, email, full_name, phone, address, postcode, is_active, email_verified, last_purchase_at) VALUES
(1, 'john.smith@email.com', 'John Smith', '07700900001', '12 Park Lane, London', 'SW1A 1AA', TRUE, TRUE, '2024-12-03 14:30:00'),
(2, 'sarah.jones@email.com', 'Sarah Jones', '07700900002', '45 High Street, Manchester', 'M1 2AB', TRUE, TRUE, '2024-12-04 10:15:00'),
(3, 'michael.brown@email.com', 'Michael Brown', '07700900003', '78 Queen Street, Birmingham', 'B1 3CD', TRUE, TRUE, '2024-12-02 16:45:00'),
(4, 'emma.wilson@email.com', 'Emma Wilson', '07700900004', '23 George Street, Glasgow', 'G1 4EF', TRUE, TRUE, '2024-12-04 11:20:00'),
(5, 'james.taylor@email.com', 'James Taylor', '07700900005', '56 Castle Street, Edinburgh', 'EH1 5GH', TRUE, TRUE, '2024-11-28 09:30:00'),
(6, 'olivia.davies@email.com', 'Olivia Davies', '07700900006', '89 Market Street, London', 'W1B 6IJ', TRUE, TRUE, '2024-12-01 13:00:00'),
(7, 'william.evans@email.com', 'William Evans', '07700900007', '34 Church Road, Manchester', 'M2 7KL', TRUE, TRUE, '2024-12-03 15:45:00'),
(8, 'sophia.thomas@email.com', 'Sophia Thomas', '07700900008', '67 Station Road, Birmingham', 'B2 8MN', TRUE, TRUE, '2024-11-30 12:30:00'),
(9, 'daniel.roberts@email.com', 'Daniel Roberts', '07700900009', '90 Bridge Street, Glasgow', 'G2 9OP', TRUE, TRUE, '2024-12-04 14:00:00'),
(10, 'emily.johnson@email.com', 'Emily Johnson', '07700900010', '12 Princess Street, Edinburgh', 'EH2 0QR', TRUE, TRUE, '2024-12-02 10:00:00'),
(11, 'alexander.white@email.com', 'Alexander White', '07700900011', '45 Victoria Road, London', 'SW1V 1ST', TRUE, TRUE, '2024-11-25 11:15:00'),
(12, 'isabella.harris@email.com', 'Isabella Harris', '07700900012', '78 Oxford Road, Manchester', 'M3 4UV', TRUE, FALSE, '2024-12-01 16:30:00'),
(13, 'thomas.martin@email.com', 'Thomas Martin', '07700900013', '23 New Street, Birmingham', 'B3 5WX', TRUE, TRUE, '2024-11-29 14:20:00'),
(14, 'charlotte.thompson@email.com', 'Charlotte Thompson', '07700900014', '56 Argyle Street, Glasgow', 'G3 6YZ', TRUE, TRUE, '2024-12-03 09:45:00'),
(15, 'joshua.garcia@email.com', 'Joshua Garcia', '07700900015', '89 Rose Street, Edinburgh', 'EH3 7AA', TRUE, TRUE, '2024-11-27 15:00:00'),
(16, 'amelia.martinez@email.com', 'Amelia Martinez', '07700900016', '34 Baker Street, London', 'NW1 8BB', TRUE, TRUE, '2024-12-04 13:30:00'),
(17, 'matthew.robinson@email.com', 'Matthew Robinson', '07700900017', '67 King Street, Manchester', 'M4 9CC', TRUE, TRUE, '2024-12-02 11:45:00'),
(18, 'mia.clark@email.com', 'Mia Clark', '07700900018', '90 Corporation Street, Birmingham', 'B4 0DD', TRUE, FALSE, '2024-11-26 10:30:00'),
(19, 'david.rodriguez@email.com', 'David Rodriguez', '07700900019', '12 Sauchiehall Street, Glasgow', 'G4 1EE', TRUE, TRUE, '2024-12-01 12:15:00'),
(20, 'harper.lewis@email.com', 'Harper Lewis', '07700900020', '45 Hanover Street, Edinburgh', 'EH5 2FF', TRUE, TRUE, '2024-12-03 16:00:00');

-- ============================================================
-- LOYALTY CARDS: 15 customers have loyalty cards with varying tiers
-- ============================================================

INSERT INTO loyalty_cards (customer_id, card_number, tier, points_balance, lifetime_points, issue_date, expiry_date, is_active) VALUES
-- Platinum tier (highest spenders)
(1, 'LC-1000000001', 'PLATINUM', 2850, 12500, '2023-01-15', '2025-01-15', TRUE),
(6, 'LC-1000000006', 'PLATINUM', 3200, 15000, '2022-11-20', '2024-11-20', TRUE),

-- Gold tier (high spenders)
(2, 'LC-1000000002', 'GOLD', 1450, 7800, '2023-03-10', '2025-03-10', TRUE),
(4, 'LC-1000000004', 'GOLD', 1680, 8500, '2023-02-05', '2025-02-05', TRUE),
(16, 'LC-1000000016', 'GOLD', 1920, 9200, '2023-04-12', '2025-04-12', TRUE),

-- Silver tier (regular customers)
(3, 'LC-1000000003', 'SILVER', 720, 3400, '2023-06-20', '2025-06-20', TRUE),
(5, 'LC-1000000005', 'SILVER', 540, 2800, '2023-07-15', '2025-07-15', TRUE),
(9, 'LC-1000000009', 'SILVER', 890, 4100, '2023-05-08', '2025-05-08', TRUE),
(14, 'LC-1000000014', 'SILVER', 650, 3200, '2023-08-22', '2025-08-22', TRUE),

-- Bronze tier (new or occasional customers)
(7, 'LC-1000000007', 'BRONZE', 180, 850, '2024-01-10', '2026-01-10', TRUE),
(10, 'LC-1000000010', 'BRONZE', 220, 1200, '2023-12-05', '2025-12-05', TRUE),
(12, 'LC-1000000012', 'BRONZE', 95, 450, '2024-02-28', '2026-02-28', TRUE),
(17, 'LC-1000000017', 'BRONZE', 310, 1450, '2024-01-20', '2026-01-20', TRUE),
(19, 'LC-1000000019', 'BRONZE', 140, 680, '2024-03-15', '2026-03-15', TRUE),
(20, 'LC-1000000020', 'BRONZE', 260, 1100, '2024-02-10', '2026-02-10', TRUE);

-- ============================================================
-- FINANCE APPROVALS: 8 customers with finance applications
-- ============================================================

INSERT INTO finance_approvals (customer_id, application_date, requested_amount, approved_amount, status, approval_date, expiry_date, credit_score, monthly_income, employment_status, amount_used, amount_remaining, approved_by) VALUES
-- Approved applications
(1, '2024-11-01 10:00:00', 5000.00, 5000.00, 'APPROVED', '2024-11-02 14:30:00', '2025-11-02', 785, 3500.00, 'FULL_TIME', 1200.00, 3800.00, 'finance_team'),
(2, '2024-11-10 11:30:00', 3000.00, 3000.00, 'APPROVED', '2024-11-11 09:15:00', '2025-11-11', 720, 2800.00, 'FULL_TIME', 450.00, 2550.00, 'finance_team'),
(4, '2024-11-15 14:00:00', 4000.00, 3500.00, 'APPROVED', '2024-11-16 10:45:00', '2025-11-16', 695, 3200.00, 'FULL_TIME', 0.00, 3500.00, 'finance_team'),
(6, '2024-10-20 09:30:00', 8000.00, 8000.00, 'APPROVED', '2024-10-21 15:00:00', '2025-10-21', 820, 4500.00, 'FULL_TIME', 2400.00, 5600.00, 'finance_team'),
(16, '2024-11-25 13:15:00', 2500.00, 2500.00, 'APPROVED', '2024-11-26 11:30:00', '2025-11-26', 710, 2600.00, 'FULL_TIME', 0.00, 2500.00, 'finance_team'),

-- Pending application
(11, '2024-12-04 10:00:00', 3500.00, NULL, 'PENDING', NULL, NULL, 680, 2900.00, 'FULL_TIME', 0.00, NULL, NULL),

-- Rejected applications
(13, '2024-11-20 15:30:00', 4000.00, NULL, 'REJECTED', '2024-11-21 12:00:00', NULL, 580, 1800.00, 'PART_TIME', 0.00, NULL, 'finance_team'),
(18, '2024-11-28 09:45:00', 2000.00, NULL, 'REJECTED', '2024-11-29 14:15:00', NULL, 540, 1500.00, 'TEMPORARY', 0.00, NULL, 'finance_team');

-- ============================================================
-- TRANSACTIONS: Sample purchase history
-- ============================================================

-- Transaction 1: John Smith (customer 1) - London store - Large DIY project with finance
INSERT INTO transactions (transaction_id, customer_id, store_id, subtotal, promotion_discount, loyalty_discount, total_amount, payment_method, finance_approved, finance_approval_ref, points_earned, points_redeemed, transaction_date) 
VALUES (1, 1, 1, 1250.00, 20.00, 30.00, 1200.00, 'FINANCE', TRUE, 'FIN-20241201-001', 120, 0, '2024-12-01 10:30:00');

INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price_original, unit_price_final, promotion_applied, line_total_original, line_total_final, line_discount) VALUES
(1, 1, 2, 84.99, 84.99, 'NONE', 169.98, 169.98, 0.00),           -- Cordless Drill x2 (London premium price)
(1, 4, 1, 129.99, 129.99, 'NONE', 129.99, 129.99, 0.00),         -- Circular Saw
(1, 15, 50, 6.99, 6.99, 'FREE_DELIVERY', 349.50, 349.50, 0.00),  -- Cement 50 bags
(1, 16, 40, 4.99, 4.99, 'FREE_DELIVERY', 199.60, 199.60, 0.00),  -- Sand 40 bags
(1, 19, 30, 8.99, 8.99, 'NONE', 269.70, 269.70, 0.00),           -- Timber x30
(1, 9, 3, 34.99, 34.99, 'THREE_FOR_TWO', 104.97, 69.98, 34.99),  -- Paint x3 (3 for 2 promo)
(1, 5, 5, 8.99, 8.99, 'NONE', 44.95, 44.95, 0.00);               -- Tape Measure x5

-- Transaction 2: Sarah Jones (customer 2) - Manchester store - BOGOF promotion
INSERT INTO transactions (transaction_id, customer_id, store_id, subtotal, promotion_discount, loyalty_discount, total_amount, payment_method, finance_approved, finance_approval_ref, points_earned, points_redeemed, transaction_date)
VALUES (2, 2, 2, 180.50, 25.98, 5.00, 149.52, 'CARD', FALSE, NULL, 15, 0, '2024-12-02 14:15:00');

INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price_original, unit_price_final, promotion_applied, line_total_original, line_total_final, line_discount) VALUES
(2, 2, 4, 12.99, 12.99, 'BOGOF', 51.96, 25.98, 25.98),           -- Hammer x4 (BOGOF: pay for 2)
(2, 5, 2, 8.99, 8.99, 'BOGOF', 17.98, 8.99, 8.99),               -- Tape Measure x2 (BOGOF)
(2, 3, 3, 22.99, 22.99, 'NONE', 68.97, 68.97, 0.00),             -- Screwdriver Set x3
(2, 6, 2, 19.99, 19.99, 'NONE', 39.98, 39.98, 0.00);             -- Level x2

-- Transaction 3: Michael Brown (customer 3) - Birmingham store - Paint and decorating
INSERT INTO transactions (transaction_id, customer_id, store_id, subtotal, promotion_discount, loyalty_discount, total_amount, payment_method, finance_approved, finance_approval_ref, points_earned, points_redeemed, transaction_date)
VALUES (3, 3, 3, 245.85, 69.96, 10.00, 165.89, 'CARD', FALSE, NULL, 17, 0, '2024-12-02 16:45:00');

INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price_original, unit_price_final, promotion_applied, line_total_original, line_total_final, line_discount) VALUES
(3, 9, 6, 34.99, 34.99, 'THREE_FOR_TWO', 209.94, 139.96, 69.98),  -- Paint x6 (3 for 2: pay for 4)
(3, 10, 3, 9.99, 9.99, 'THREE_FOR_TWO', 29.97, 19.98, 9.99),      -- Paint Roller x3 (3 for 2)
(3, 11, 2, 14.99, 14.99, 'NONE', 29.98, 29.98, 0.00),             -- Paintbrush Set x2
(3, 12, 5, 3.99, 3.99, 'NONE', 19.95, 19.95, 0.00);               -- Masking Tape x5

-- Transaction 4: Emma Wilson (customer 4) - Glasgow store - Garden equipment
INSERT INTO transactions (transaction_id, customer_id, store_id, subtotal, promotion_discount, loyalty_discount, total_amount, payment_method, finance_approved, finance_approval_ref, points_earned, points_redeemed, transaction_date)
VALUES (4, 4, 4, 315.95, 20.00, 15.00, 280.95, 'CARD', FALSE, NULL, 28, 0, '2024-12-03 11:20:00');

INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price_original, unit_price_final, promotion_applied, line_total_original, line_total_final, line_discount) VALUES
(4, 27, 1, 139.99, 119.99, 'FIXED_DISCOUNT', 139.99, 119.99, 20.00),  -- Lawn Mower (£20 off)
(4, 26, 2, 19.99, 19.99, 'NONE', 39.98, 39.98, 0.00),                 -- Garden Hose x2
(4, 28, 3, 16.99, 16.99, 'NONE', 50.97, 50.97, 0.00),                 -- Spade x3
(4, 29, 1, 44.99, 44.99, 'NONE', 44.99, 44.99, 0.00),                 -- Wheelbarrow
(4, 30, 8, 6.99, 6.99, 'NONE', 55.92, 55.92, 0.00);                   -- Compost x8

-- Transaction 5: Olivia Davies (customer 6) - London store - Large finance purchase
INSERT INTO transactions (transaction_id, customer_id, store_id, subtotal, promotion_discount, loyalty_discount, total_amount, payment_method, finance_approved, finance_approval_ref, points_earned, points_redeemed, transaction_date)
VALUES (5, 6, 1, 2450.00, 40.00, 50.00, 2360.00, 'FINANCE', TRUE, 'FIN-20241203-002', 236, 0, '2024-12-03 13:00:00');

INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price_original, unit_price_final, promotion_applied, line_total_original, line_total_final, line_discount) VALUES
(5, 4, 3, 129.99, 129.99, 'NONE', 389.97, 389.97, 0.00),          -- Circular Saw x3
(5, 1, 5, 84.99, 84.99, 'NONE', 424.95, 424.95, 0.00),            -- Cordless Drill x5
(5, 7, 4, 54.99, 54.99, 'NONE', 219.96, 219.96, 0.00),            -- Angle Grinder x4
(5, 20, 20, 29.99, 29.99, 'NONE', 599.80, 599.80, 0.00),          -- Plywood x20
(5, 18, 40, 12.99, 12.99, 'NONE', 519.60, 519.60, 0.00),          -- Plasterboard x40
(5, 9, 9, 34.99, 34.99, 'THREE_FOR_TWO', 314.91, 209.94, 104.97); -- Paint x9 (3 for 2: pay for 6)

-- Transaction 6: William Evans (customer 7) - Manchester store - Small purchase
INSERT INTO transactions (transaction_id, customer_id, store_id, subtotal, promotion_discount, loyalty_discount, total_amount, payment_method, finance_approved, finance_approval_ref, points_earned, points_redeemed, transaction_date)
VALUES (6, 7, 2, 85.94, 8.99, 2.00, 74.95, 'CASH', FALSE, NULL, 7, 0, '2024-12-03 15:45:00');

INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price_original, unit_price_final, promotion_applied, line_total_original, line_total_final, line_discount) VALUES
(6, 2, 2, 12.99, 12.99, 'BOGOF', 25.98, 12.99, 12.99),            -- Hammer x2 (BOGOF)
(6, 3, 2, 22.99, 22.99, 'NONE', 45.98, 45.98, 0.00),              -- Screwdriver Set x2
(6, 12, 5, 3.99, 3.99, 'NONE', 19.95, 19.95, 0.00);               -- Masking Tape x5

-- Transaction 7: Daniel Roberts (customer 9) - Glasgow store - Building materials
INSERT INTO transactions (transaction_id, customer_id, store_id, subtotal, promotion_discount, loyalty_discount, total_amount, payment_method, finance_approved, finance_approval_ref, points_earned, points_redeemed, transaction_date)
VALUES (7, 9, 4, 580.00, 0.00, 20.00, 560.00, 'CARD', FALSE, NULL, 56, 0, '2024-12-04 09:30:00');

INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price_original, unit_price_final, promotion_applied, line_total_original, line_total_final, line_discount) VALUES
(7, 15, 60, 6.99, 6.99, 'FREE_DELIVERY', 419.40, 419.40, 0.00),   -- Cement x60
(7, 16, 30, 4.99, 4.99, 'FREE_DELIVERY', 149.70, 149.70, 0.00),   -- Sand x30
(7, 23, 2, 4.49, 4.49, 'NONE', 8.98, 8.98, 0.00);                 -- Wall Plugs x2

-- Transaction 8: Emily Johnson (customer 10) - Edinburgh store - Hardware
INSERT INTO transactions (transaction_id, customer_id, store_id, subtotal, promotion_discount, loyalty_discount, total_amount, payment_method, finance_approved, finance_approval_ref, points_earned, points_redeemed, transaction_date)
VALUES (8, 10, 5, 125.40, 0.00, 5.00, 120.40, 'CARD', FALSE, NULL, 12, 0, '2024-12-04 10:00:00');

INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price_original, unit_price_final, promotion_applied, line_total_original, line_total_final, line_discount) VALUES
(8, 21, 10, 7.99, 7.99, 'NONE', 79.90, 79.90, 0.00),              -- Screws x10
(8, 22, 5, 5.99, 5.99, 'NONE', 29.95, 29.95, 0.00),               -- Nails x5
(8, 24, 2, 8.99, 8.99, 'NONE', 17.98, 17.98, 0.00);               -- Hinges x2

-- Transaction 9: Amelia Martinez (customer 16) - London store - Premium tools
INSERT INTO transactions (transaction_id, customer_id, store_id, subtotal, promotion_discount, loyalty_discount, total_amount, payment_method, finance_approved, finance_approval_ref, points_earned, points_redeemed, transaction_date)
VALUES (9, 16, 1, 455.90, 10.00, 20.00, 425.90, 'CARD', FALSE, NULL, 43, 0, '2024-12-04 13:30:00');

INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price_original, unit_price_final, promotion_applied, line_total_original, line_total_final, line_discount) VALUES
(9, 1, 3, 84.99, 76.49, 'PERCENTAGE_OFF', 254.97, 229.47, 25.50), -- Cordless Drill x3 (10% off)
(9, 4, 1, 129.99, 129.99, 'NONE', 129.99, 129.99, 0.00),          -- Circular Saw
(9, 7, 2, 54.99, 54.99, 'NONE', 109.98, 109.98, 0.00);            -- Angle Grinder x2

-- Transaction 10: Matthew Robinson (customer 17) - Manchester store - Mixed purchase
INSERT INTO transactions (transaction_id, customer_id, store_id, subtotal, promotion_discount, loyalty_discount, total_amount, payment_method, finance_approved, finance_approval_ref, points_earned, points_redeemed, transaction_date)
VALUES (10, 17, 2, 220.80, 34.97, 8.00, 177.83, 'CARD', FALSE, NULL, 18, 0, '2024-12-04 11:45:00');

INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price_original, unit_price_final, promotion_applied, line_total_original, line_total_final, line_discount) VALUES
(10, 9, 3, 34.99, 34.99, 'THREE_FOR_TWO', 104.97, 69.98, 34.99),  -- Paint x3 (3 for 2)
(10, 2, 2, 12.99, 12.99, 'BOGOF', 25.98, 12.99, 12.99),           -- Hammer x2 (BOGOF)
(10, 6, 3, 19.99, 19.99, 'NONE', 59.97, 59.97, 0.00),             -- Level x3
(10, 8, 4, 16.99, 16.99, 'NONE', 67.96, 67.96, 0.00);             -- Pliers x4

-- ============================================================
-- LOYALTY POINTS HISTORY: Audit trail for points transactions
-- ============================================================

-- Points earned from transactions
INSERT INTO loyalty_points_history (card_id, transaction_id, points_change, points_type, balance_before, balance_after, description) VALUES
-- Customer 1 (Platinum)
(1, 1, 120, 'EARNED', 2730, 2850, 'Purchase transaction'),

-- Customer 2 (Gold)
(2, 2, 15, 'EARNED', 1435, 1450, 'Purchase transaction'),

-- Customer 3 (Silver)
(3, 3, 17, 'EARNED', 703, 720, 'Purchase transaction'),

-- Customer 4 (Gold)
(4, 4, 28, 'EARNED', 1652, 1680, 'Purchase transaction'),

-- Customer 6 (Platinum)
(5, 5, 236, 'EARNED', 2964, 3200, 'Purchase transaction'),

-- Customer 7 (Bronze)
(6, 6, 7, 'EARNED', 173, 180, 'Purchase transaction'),

-- Customer 9 (Silver)
(7, 7, 56, 'EARNED', 834, 890, 'Purchase transaction'),

-- Customer 10 (Bronze)
(8, 8, 12, 'EARNED', 208, 220, 'Purchase transaction'),

-- Customer 16 (Gold)
(9, 9, 43, 'EARNED', 1877, 1920, 'Purchase transaction'),

-- Customer 17 (Bronze)
(10, 10, 18, 'EARNED', 292, 310, 'Purchase transaction'),

-- Bonus points and adjustments
(1, NULL, 500, 'BONUS', 2350, 2850, 'Platinum tier anniversary bonus'),
(5, NULL, 1000, 'BONUS', 2200, 3200, 'Platinum tier upgrade bonus'),
(2, NULL, 200, 'BONUS', 1250, 1450, 'Birthday bonus'),

-- Points redeemed (example)
(3, NULL, -50, 'REDEEMED', 770, 720, 'Redeemed for discount'),
(7, NULL, -30, 'REDEEMED', 920, 890, 'Redeemed for discount');

-- Update last_purchase_at for customers based on their latest transaction
UPDATE customers SET last_purchase_at = '2024-12-01 10:30:00' WHERE customer_id = 1;
UPDATE customers SET last_purchase_at = '2024-12-02 14:15:00' WHERE customer_id = 2;
UPDATE customers SET last_purchase_at = '2024-12-02 16:45:00' WHERE customer_id = 3;
UPDATE customers SET last_purchase_at = '2024-12-03 11:20:00' WHERE customer_id = 4;
UPDATE customers SET last_purchase_at = '2024-12-03 13:00:00' WHERE customer_id = 6;
UPDATE customers SET last_purchase_at = '2024-12-03 15:45:00' WHERE customer_id = 7;
UPDATE customers SET last_purchase_at = '2024-12-04 09:30:00' WHERE customer_id = 9;
UPDATE customers SET last_purchase_at = '2024-12-04 10:00:00' WHERE customer_id = 10;
UPDATE customers SET last_purchase_at = '2024-12-04 13:30:00' WHERE customer_id = 16;
UPDATE customers SET last_purchase_at = '2024-12-04 11:45:00' WHERE customer_id = 17;

-- Verify data integrity
SELECT 'Customers' as table_name, COUNT(*) as record_count FROM customers
UNION ALL
SELECT 'Loyalty Cards', COUNT(*) FROM loyalty_cards
UNION ALL
SELECT 'Transactions', COUNT(*) FROM transactions
UNION ALL
SELECT 'Transaction Items', COUNT(*) FROM transaction_items
UNION ALL
SELECT 'Finance Approvals', COUNT(*) FROM finance_approvals
UNION ALL
SELECT 'Points History', COUNT(*) FROM loyalty_points_history;

-- Display summary statistics
SELECT * FROM customer_summary ORDER BY lifetime_spending DESC LIMIT 10;
SELECT * FROM store_performance ORDER BY total_revenue DESC;

