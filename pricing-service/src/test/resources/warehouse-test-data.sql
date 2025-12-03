-- Test data initialization for warehouse database (H2)
-- Subset of production data for testing

CREATE TABLE IF NOT EXISTS items (
    item_id INT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    base_price DECIMAL(10,2) NOT NULL,
    stock_quantity INT NOT NULL
);

-- Insert test items
INSERT INTO items (item_id, name, category, base_price, stock_quantity) VALUES
(1, 'Cordless Drill', 'Power Tools', 79.99, 50),
(2, 'Hammer', 'Hand Tools', 12.99, 100),
(3, 'Paint Roller Set', 'Paint Supplies', 8.99, 75),
(4, 'Screwdriver Set', 'Hand Tools', 24.99, 60),
(5, 'Extension Ladder', 'Ladders', 149.99, 20),
(6, 'Safety Goggles', 'Safety Equipment', 5.99, 200),
(7, 'Measuring Tape', 'Measuring Tools', 9.99, 80),
(8, 'Garden Hose', 'Garden Equipment', 29.99, 40),
(9, 'Work Gloves', 'Safety Equipment', 7.99, 150),
(10, 'Electric Sander', 'Power Tools', 89.99, 30);
