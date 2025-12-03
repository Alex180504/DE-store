-- Warehouse Database Schema (Legacy MySQL)
-- This is a READ-ONLY database for DE-Store system

CREATE TABLE IF NOT EXISTS items (
    item_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    base_price DECIMAL(10,2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Sample Data: Home Improvement & DIY Products (30 items)
INSERT INTO items (item_id, name, category, base_price, stock_quantity) VALUES
-- Tools
(1, 'Cordless Drill 18V', 'Power Tools', 89.99, 45),
(2, 'Hammer Claw 16oz', 'Hand Tools', 12.99, 120),
(3, 'Screwdriver Set 20pc', 'Hand Tools', 24.99, 85),
(4, 'Circular Saw 7.25in', 'Power Tools', 129.99, 30),
(5, 'Tape Measure 25ft', 'Hand Tools', 8.99, 200),
(6, 'Level Spirit 24in', 'Hand Tools', 19.99, 65),
(7, 'Angle Grinder 4.5in', 'Power Tools', 54.99, 38),
(8, 'Pliers Set 3pc', 'Hand Tools', 16.99, 95),

-- Paint & Decorating
(9, 'Emulsion Paint White 10L', 'Paint', 34.99, 150),
(10, 'Paint Roller Set', 'Paint Accessories', 9.99, 180),
(11, 'Paintbrush Set 5pc', 'Paint Accessories', 14.99, 140),
(12, 'Masking Tape 50m', 'Paint Accessories', 3.99, 250),
(13, 'Gloss Paint Black 2.5L', 'Paint', 22.99, 75),
(14, 'Wood Stain Oak 750ml', 'Paint', 18.99, 60),

-- Building Materials
(15, 'Cement 25kg Bag', 'Building Materials', 6.99, 300),
(16, 'Sand Builder 25kg', 'Building Materials', 4.99, 280),
(17, 'Gravel 20kg Bag', 'Building Materials', 5.49, 200),
(18, 'Plasterboard 1200x2400mm', 'Building Materials', 12.99, 100),
(19, 'Timber 2x4 8ft', 'Lumber', 8.99, 180),
(20, 'Plywood Sheet 8x4ft', 'Lumber', 29.99, 55),

-- Hardware & Fixings
(21, 'Screws Wood 200pk', 'Fixings', 7.99, 320),
(22, 'Nails Assorted 500g', 'Fixings', 5.99, 280),
(23, 'Wall Plugs 100pk', 'Fixings', 4.49, 400),
(24, 'Hinges Brass 2pk', 'Hardware', 8.99, 150),
(25, 'Door Handle Chrome', 'Hardware', 24.99, 85),

-- Garden & Outdoor
(26, 'Garden Hose 30m', 'Garden', 19.99, 70),
(27, 'Lawn Mower Electric', 'Garden Equipment', 149.99, 25),
(28, 'Spade Digging', 'Garden Tools', 16.99, 90),
(29, 'Wheelbarrow 85L', 'Garden Equipment', 44.99, 40),
(30, 'Compost 50L Bag', 'Garden', 6.99, 200);

-- Create a read-only user view for reporting purposes
CREATE OR REPLACE VIEW items_summary AS
SELECT 
    category,
    COUNT(*) as item_count,
    AVG(base_price) as avg_price,
    SUM(stock_quantity) as total_stock
FROM items
GROUP BY category;
