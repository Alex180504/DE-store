-- Sample Transaction Data for Accounting Database
-- Consistent with loyalty-db product rules and store data

-- ============================================================
-- SAMPLE TRANSACTIONS
-- ============================================================
-- Customers: 1-10 (matching loyalty system)
-- Stores: 1 (London), 2, 3
-- Items: 1-29 (matching product_points_rules in loyalty-db)
-- Dates: Last 3 months of transactions
-- Amounts: Varied to test different bonus tiers

-- Customer 1: High-value customer with multiple purchases
INSERT INTO transactions (customer_id, store_id, transaction_date, total_amount, status, payment_method) VALUES
(1, 1, CURRENT_TIMESTAMP - INTERVAL '89 days', 145.50, 'COMPLETED', 'CREDIT_CARD'),
(1, 1, CURRENT_TIMESTAMP - INTERVAL '75 days', 320.00, 'COMPLETED', 'CREDIT_CARD'),
(1, 1, CURRENT_TIMESTAMP - INTERVAL '45 days', 89.99, 'COMPLETED', 'DEBIT_CARD'),
(1, 1, CURRENT_TIMESTAMP - INTERVAL '30 days', 215.75, 'COMPLETED', 'CREDIT_CARD'),
(1, 1, CURRENT_TIMESTAMP - INTERVAL '15 days', 67.50, 'COMPLETED', 'CASH'),
(1, 1, CURRENT_TIMESTAMP - INTERVAL '5 days', 450.00, 'COMPLETED', 'CREDIT_CARD');

-- Customer 2: Regular customer
INSERT INTO transactions (customer_id, store_id, transaction_date, total_amount, status, payment_method) VALUES
(2, 1, CURRENT_TIMESTAMP - INTERVAL '80 days', 95.00, 'COMPLETED', 'DEBIT_CARD'),
(2, 2, CURRENT_TIMESTAMP - INTERVAL '60 days', 125.50, 'COMPLETED', 'CREDIT_CARD'),
(2, 1, CURRENT_TIMESTAMP - INTERVAL '40 days', 78.25, 'COMPLETED', 'DEBIT_CARD'),
(2, 1, CURRENT_TIMESTAMP - INTERVAL '20 days', 110.00, 'COMPLETED', 'CREDIT_CARD');

-- Customer 3: Occasional shopper
INSERT INTO transactions (customer_id, store_id, transaction_date, total_amount, status, payment_method) VALUES
(3, 2, CURRENT_TIMESTAMP - INTERVAL '70 days', 55.00, 'COMPLETED', 'CASH'),
(3, 2, CURRENT_TIMESTAMP - INTERVAL '35 days', 220.00, 'COMPLETED', 'CREDIT_CARD'),
(3, 2, CURRENT_TIMESTAMP - INTERVAL '10 days', 95.50, 'COMPLETED', 'DEBIT_CARD');

-- Customer 4: New customer with recent large purchase
INSERT INTO transactions (customer_id, store_id, transaction_date, total_amount, status, payment_method) VALUES
(4, 3, CURRENT_TIMESTAMP - INTERVAL '25 days', 580.00, 'COMPLETED', 'CREDIT_CARD'),
(4, 3, CURRENT_TIMESTAMP - INTERVAL '8 days', 45.99, 'COMPLETED', 'DEBIT_CARD');

-- Customer 5: DIY enthusiast
INSERT INTO transactions (customer_id, store_id, transaction_date, total_amount, status, payment_method) VALUES
(5, 1, CURRENT_TIMESTAMP - INTERVAL '85 days', 155.00, 'COMPLETED', 'DEBIT_CARD'),
(5, 1, CURRENT_TIMESTAMP - INTERVAL '65 days', 89.50, 'COMPLETED', 'CREDIT_CARD'),
(5, 1, CURRENT_TIMESTAMP - INTERVAL '50 days', 210.00, 'COMPLETED', 'CREDIT_CARD'),
(5, 1, CURRENT_TIMESTAMP - INTERVAL '28 days', 75.25, 'COMPLETED', 'DEBIT_CARD');

-- Customer 6: Garden supplies customer
INSERT INTO transactions (customer_id, store_id, transaction_date, total_amount, status, payment_method) VALUES
(6, 2, CURRENT_TIMESTAMP - INTERVAL '78 days', 120.00, 'COMPLETED', 'CASH'),
(6, 2, CURRENT_TIMESTAMP - INTERVAL '42 days', 165.50, 'COMPLETED', 'DEBIT_CARD'),
(6, 2, CURRENT_TIMESTAMP - INTERVAL '12 days', 89.99, 'COMPLETED', 'CREDIT_CARD');

-- Customer 7: Budget shopper
INSERT INTO transactions (customer_id, store_id, transaction_date, total_amount, status, payment_method) VALUES
(7, 3, CURRENT_TIMESTAMP - INTERVAL '68 days', 35.50, 'COMPLETED', 'CASH'),
(7, 3, CURRENT_TIMESTAMP - INTERVAL '48 days', 52.00, 'COMPLETED', 'DEBIT_CARD'),
(7, 3, CURRENT_TIMESTAMP - INTERVAL '22 days', 48.75, 'COMPLETED', 'CASH');

-- Customer 8: Professional contractor
INSERT INTO transactions (customer_id, store_id, transaction_date, total_amount, status, payment_method) VALUES
(8, 1, CURRENT_TIMESTAMP - INTERVAL '82 days', 425.00, 'COMPLETED', 'CREDIT_CARD'),
(8, 1, CURRENT_TIMESTAMP - INTERVAL '55 days', 310.50, 'COMPLETED', 'CREDIT_CARD'),
(8, 1, CURRENT_TIMESTAMP - INTERVAL '18 days', 275.00, 'COMPLETED', 'CREDIT_CARD');

-- Customer 9: Moderate customer
INSERT INTO transactions (customer_id, store_id, transaction_date, total_amount, status, payment_method) VALUES
(9, 2, CURRENT_TIMESTAMP - INTERVAL '72 days', 105.00, 'COMPLETED', 'DEBIT_CARD'),
(9, 2, CURRENT_TIMESTAMP - INTERVAL '38 days', 140.25, 'COMPLETED', 'CREDIT_CARD');

-- Customer 10: Recent customer
INSERT INTO transactions (customer_id, store_id, transaction_date, total_amount, status, payment_method) VALUES
(10, 3, CURRENT_TIMESTAMP - INTERVAL '62 days', 88.50, 'COMPLETED', 'DEBIT_CARD'),
(10, 3, CURRENT_TIMESTAMP - INTERVAL '32 days', 115.00, 'COMPLETED', 'CREDIT_CARD'),
(10, 3, CURRENT_TIMESTAMP - INTERVAL '6 days', 92.75, 'COMPLETED', 'DEBIT_CARD');

-- Some cancelled/refunded transactions
INSERT INTO transactions (customer_id, store_id, transaction_date, total_amount, status, payment_method) VALUES
(1, 1, CURRENT_TIMESTAMP - INTERVAL '52 days', 75.00, 'CANCELLED', 'CREDIT_CARD'),
(3, 2, CURRENT_TIMESTAMP - INTERVAL '24 days', 105.00, 'REFUNDED', 'DEBIT_CARD');

-- ============================================================
-- SAMPLE TRANSACTION ITEMS
-- ============================================================
-- Item IDs match product_points_rules in loyalty-db:
-- Power Tools: 1-6 (drills, saws, sanders, routers, grinders, impact drivers)
-- Hand Tools: 7-12 (hammers, screwdrivers, wrenches, pliers, tape measures, levels)
-- Paint: 13-15 (emulsion, gloss, masonry)
-- Building Materials: 16-20 (timber, plasterboard, cement, bricks, insulation)
-- Garden Equipment: 21-29 (mowers, strimmers, spades, rakes, pruners, hoses, wheelbarrows, compost, plant pots)

-- Transaction 1: Customer 1 - Mixed purchase (power tools and paint)
INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price, line_total) VALUES
(1, 1, 1, 89.99, 89.99),   -- Cordless Drill
(1, 13, 2, 24.99, 49.98),  -- Emulsion Paint
(1, 10, 1, 5.50, 5.50);    -- Pliers

-- Transaction 2: Customer 1 - Large purchase (building materials)
INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price, line_total) VALUES
(2, 16, 10, 12.50, 125.00), -- Timber
(2, 17, 8, 8.75, 70.00),    -- Plasterboard
(2, 18, 5, 25.00, 125.00);  -- Cement

-- Transaction 3: Customer 1 - Hand tools
INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price, line_total) VALUES
(3, 7, 2, 18.50, 37.00),    -- Hammer
(3, 8, 3, 12.99, 38.97),    -- Screwdriver Set
(3, 11, 1, 14.00, 14.00);   -- Tape Measure

-- Transaction 4: Customer 1 - Garden equipment
INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price, line_total) VALUES
(4, 21, 1, 185.00, 185.00), -- Lawn Mower
(4, 23, 1, 15.75, 15.75),   -- Spade
(4, 26, 1, 14.99, 14.99);   -- Garden Hose

-- Transaction 5: Customer 1 - Paint and supplies
INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price, line_total) VALUES
(5, 14, 2, 28.50, 57.00),   -- Gloss Paint
(5, 9, 1, 10.50, 10.50);    -- Wrench Set

-- Transaction 6: Customer 1 - Power tools (high value)
INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price, line_total) VALUES
(6, 2, 1, 225.00, 225.00),  -- Circular Saw
(6, 4, 1, 175.00, 175.00),  -- Router
(6, 5, 1, 50.00, 50.00);    -- Angle Grinder

-- Transaction 7: Customer 2 - DIY starter kit
INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price, line_total) VALUES
(7, 7, 1, 18.50, 18.50),    -- Hammer
(7, 8, 1, 25.99, 25.99),    -- Screwdriver Set
(7, 10, 1, 5.50, 5.50),     -- Pliers
(7, 11, 1, 14.00, 14.00),   -- Tape Measure
(7, 12, 1, 31.00, 31.00);   -- Spirit Level

-- Transaction 8: Customer 2 - Building project
INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price, line_total) VALUES
(8, 16, 5, 12.50, 62.50),   -- Timber
(8, 18, 2, 25.00, 50.00),   -- Cement
(8, 13, 1, 12.99, 12.99);   -- Emulsion Paint

-- Transaction 9: Customer 2 - Maintenance
INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price, line_total) VALUES
(9, 14, 1, 28.50, 28.50),   -- Gloss Paint
(9, 13, 2, 24.99, 49.98);   -- Emulsion Paint

-- Transaction 10: Customer 2 - Tool upgrade
INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price, line_total) VALUES
(10, 6, 1, 95.00, 95.00),   -- Impact Driver
(10, 15, 1, 14.99, 14.99);  -- Masonry Paint

-- Transaction 11: Customer 3 - Garden supplies
INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price, line_total) VALUES
(11, 28, 5, 6.50, 32.50),   -- Compost Bag
(11, 29, 10, 2.25, 22.50);  -- Plant Pots

-- Transaction 12: Customer 3 - Garden tools
INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price, line_total) VALUES
(12, 21, 1, 185.00, 185.00), -- Lawn Mower
(12, 22, 1, 35.00, 35.00);   -- Strimmer

-- Transaction 13: Customer 3 - Hand tools
INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price, line_total) VALUES
(13, 7, 2, 18.50, 37.00),    -- Hammer
(13, 8, 1, 25.99, 25.99),    -- Screwdriver Set
(13, 9, 1, 32.50, 32.50);    -- Wrench Set

-- Transaction 14: Customer 4 - Major renovation (high value)
INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price, line_total) VALUES
(14, 2, 1, 225.00, 225.00),  -- Circular Saw
(14, 16, 15, 12.50, 187.50), -- Timber
(14, 17, 10, 8.75, 87.50),   -- Plasterboard
(14, 18, 4, 25.00, 100.00);  -- Cement

-- Transaction 15: Customer 4 - Small purchase
INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price, line_total) VALUES
(15, 13, 1, 24.99, 24.99),   -- Emulsion Paint
(15, 11, 1, 14.00, 14.00),   -- Tape Measure
(15, 10, 1, 6.99, 6.99);     -- Pliers

-- Transaction 16: Customer 5 - Power tools
INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price, line_total) VALUES
(16, 1, 1, 89.99, 89.99),    -- Cordless Drill
(16, 3, 1, 65.00, 65.00);    -- Orbital Sander

-- Transaction 17: Customer 5 - Paint supplies
INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price, line_total) VALUES
(17, 13, 2, 24.99, 49.98),   -- Emulsion Paint
(17, 14, 1, 28.50, 28.50),   -- Gloss Paint
(17, 12, 1, 10.99, 10.99);   -- Spirit Level

-- Transaction 18: Customer 5 - Building materials
INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price, line_total) VALUES
(18, 16, 8, 12.50, 100.00),  -- Timber
(18, 19, 50, 1.25, 62.50),   -- Bricks
(18, 20, 5, 9.50, 47.50);    -- Insulation

-- Transaction 19: Customer 5 - Maintenance
INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price, line_total) VALUES
(19, 15, 2, 14.99, 29.98),   -- Masonry Paint
(19, 7, 1, 18.50, 18.50),    -- Hammer
(19, 26, 1, 26.75, 26.75);   -- Garden Hose

-- Transaction 20: Customer 6 - Garden equipment
INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price, line_total) VALUES
(20, 21, 1, 120.00, 120.00); -- Lawn Mower

-- Transaction 21: Customer 6 - Garden tools and supplies
INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price, line_total) VALUES
(21, 23, 1, 15.75, 15.75),   -- Spade
(21, 24, 1, 12.50, 12.50),   -- Rake
(21, 27, 1, 45.00, 45.00),   -- Wheelbarrow
(21, 28, 10, 6.50, 65.00),   -- Compost
(21, 29, 15, 1.80, 27.00);   -- Plant Pots

-- Transaction 22: Customer 6 - Power tools
INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price, line_total) VALUES
(22, 22, 1, 35.00, 35.00),   -- Strimmer
(22, 25, 1, 54.99, 54.99);   -- Pruners

-- Transaction 23: Customer 7 - Budget hand tools
INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price, line_total) VALUES
(23, 7, 1, 15.50, 15.50),    -- Hammer
(23, 10, 2, 5.00, 10.00),    -- Pliers
(23, 11, 1, 9.99, 9.99);     -- Tape Measure

-- Transaction 24: Customer 7 - Paint
INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price, line_total) VALUES
(24, 13, 2, 24.99, 49.98),   -- Emulsion Paint
(24, 8, 1, 2.00, 2.00);      -- Screwdriver

-- Transaction 25: Customer 7 - Small supplies
INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price, line_total) VALUES
(25, 19, 20, 1.25, 25.00),   -- Bricks
(25, 13, 1, 23.75, 23.75);   -- Emulsion Paint

-- Transaction 26: Customer 8 - Professional power tools (high value)
INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price, line_total) VALUES
(26, 2, 1, 225.00, 225.00),  -- Circular Saw
(26, 4, 1, 175.00, 175.00),  -- Router
(26, 6, 1, 24.99, 24.99);    -- Impact Driver

-- Transaction 27: Customer 8 - Building materials
INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price, line_total) VALUES
(27, 16, 20, 12.50, 250.00), -- Timber
(27, 18, 5, 12.10, 60.50);   -- Cement

-- Transaction 28: Customer 8 - Supplies
INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price, line_total) VALUES
(28, 17, 15, 8.75, 131.25),  -- Plasterboard
(28, 20, 10, 9.50, 95.00),   -- Insulation
(28, 13, 2, 24.37, 48.75);   -- Emulsion Paint

-- Transaction 29: Customer 9 - DIY project
INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price, line_total) VALUES
(29, 1, 1, 89.99, 89.99),    -- Cordless Drill
(29, 14, 1, 14.99, 14.99);   -- Gloss Paint

-- Transaction 30: Customer 9 - Garden and paint
INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price, line_total) VALUES
(30, 23, 1, 15.75, 15.75),   -- Spade
(30, 28, 10, 6.50, 65.00),   -- Compost
(30, 13, 3, 19.83, 59.50);   -- Emulsion Paint

-- Transaction 31: Customer 10 - Hand tools
INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price, line_total) VALUES
(31, 7, 2, 18.50, 37.00),    -- Hammer
(31, 8, 2, 12.99, 25.99),    -- Screwdriver Set
(31, 9, 1, 25.50, 25.50);    -- Wrench Set

-- Transaction 32: Customer 10 - Power tool
INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price, line_total) VALUES
(32, 3, 1, 65.00, 65.00),    -- Orbital Sander
(32, 13, 2, 24.99, 49.98);   -- Emulsion Paint

-- Transaction 33: Customer 10 - Garden supplies
INSERT INTO transaction_items (transaction_id, item_id, quantity, unit_price, line_total) VALUES
(33, 24, 1, 12.50, 12.50),   -- Rake
(33, 28, 5, 6.50, 32.50),    -- Compost
(33, 29, 20, 2.38, 47.75);   -- Plant Pots

-- Cancelled/Refunded transactions (no items needed as they weren't completed)
