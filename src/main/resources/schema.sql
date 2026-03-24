-- schema.sql
-- Database: kitchen_order_ticket
CREATE DATABASE IF NOT EXISTS kitchen_order_ticket;
USE kitchen_order_ticket;

-- Table to store user accounts
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    is_first_login BOOLEAN DEFAULT TRUE
);

-- Seed default owner account
INSERT IGNORE INTO users (name, username, password, role, is_first_login) VALUES ('Restaurant Owner', 'owner', 'owner123', 'Owner', FALSE);

-- Table to store the restaurant menu
CREATE TABLE IF NOT EXISTS menu_items (
    item_code INT PRIMARY KEY,
    item_name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    price DOUBLE NOT NULL,
    portion VARCHAR(50) NOT NULL
);

-- Table to log completed orders for revenue and performance tracking
CREATE TABLE IF NOT EXISTS completed_orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    waiter_name VARCHAR(50) NOT NULL,
    chef_name VARCHAR(50),
    table_number INT NOT NULL,
    total_amount DOUBLE NOT NULL,
    status VARCHAR(20) DEFAULT 'COMPLETED',
    rejection_reason VARCHAR(255),
    order_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Seed Data: 50+ diverse menu items
-- Format: Code, Name, Category, Price, Portion
INSERT IGNORE INTO menu_items (item_code, item_name, category, price, portion) VALUES
-- Starters (101-110)
(101, 'Paneer Tikka', 'Starters', 250.00, '1 Plate (6 pcs)'),
(102, 'Chicken Tikka', 'Starters', 300.00, '1 Plate (6 pcs)'),
(103, 'Samosa', 'Starters', 50.00, '2 pcs'),
(104, 'Spring Rolls', 'Starters', 150.00, '4 pcs'),
(105, 'Manchow Soup', 'Starters', 120.00, '1 Bowl'),
(106, 'Tomato Soup', 'Starters', 100.00, '1 Bowl'),
(107, 'Fish Fingers', 'Starters', 350.00, '1 Plate (8 pcs)'),
(108, 'Crispy Corn', 'Starters', 180.00, '1 Plate'),
(109, 'Hara Bhara Kebab', 'Starters', 220.00, '6 pcs'),
(110, 'Mutton Seekh Kebab', 'Starters', 400.00, '4 pcs'),

-- Veg Main Course (201-215)
(201, 'Dal Makhani', 'Veg', 220.00, '1 Bowl (Serves 2)'),
(202, 'Paneer Butter Masala', 'Veg', 280.00, '1 Bowl (Serves 2)'),
(203, 'Palak Paneer', 'Veg', 260.00, '1 Bowl (Serves 2)'),
(204, 'Mixed Veg Curry', 'Veg', 200.00, '1 Bowl'),
(205, 'Aloo Gobi', 'Veg', 180.00, '1 Bowl'),
(206, 'Malai Kofta', 'Veg', 290.00, '1 Bowl (4 pcs)'),
(207, 'Chana Masala', 'Veg', 190.00, '1 Bowl'),
(208, 'Bhindi Masala', 'Veg', 170.00, '1 Bowl'),
(209, 'Veg Jalfrezi', 'Veg', 210.00, '1 Bowl'),
(210, 'Mushroom Matar', 'Veg', 250.00, '1 Bowl'),
(211, 'Kadai Paneer', 'Veg', 270.00, '1 Bowl'),
(212, 'Navratan Korma', 'Veg', 300.00, '1 Bowl'),
(213, 'Baingan Bharta', 'Veg', 160.00, '1 Bowl'),
(214, 'Dum Aloo', 'Veg', 190.00, '1 Bowl'),
(215, 'Yellow Dal Tadka', 'Veg', 150.00, '1 Bowl'),

-- Non-Veg Main Course (301-315)
(301, 'Butter Chicken', 'Non-Veg', 350.00, '1 Bowl (Serves 2)'),
(302, 'Chicken Curry', 'Non-Veg', 320.00, '1 Bowl'),
(303, 'Kadai Chicken', 'Non-Veg', 330.00, '1 Bowl'),
(304, 'Mutton Rogan Josh', 'Non-Veg', 450.00, '1 Bowl'),
(305, 'Mutton Curry', 'Non-Veg', 420.00, '1 Bowl'),
(306, 'Fish Curry', 'Non-Veg', 380.00, '1 Bowl'),
(307, 'Prawn Masala', 'Non-Veg', 450.00, '1 Bowl'),
(308, 'Chicken Tikka Masala', 'Non-Veg', 360.00, '1 Bowl'),
(309, 'Egg Curry', 'Non-Veg', 180.00, '1 Bowl (2 Eggs)'),
(310, 'Chicken Korma', 'Non-Veg', 340.00, '1 Bowl'),
(311, 'Keema Matar', 'Non-Veg', 400.00, '1 Bowl'),
(312, 'Goan Fish Curry', 'Non-Veg', 400.00, '1 Bowl'),
(313, 'Chicken Do Pyaza', 'Non-Veg', 330.00, '1 Bowl'),
(314, 'Mutton Bhuna Gosht', 'Non-Veg', 460.00, '1 Bowl'),
(315, 'Chilli Chicken Dry', 'Non-Veg', 310.00, '1 Plate'),

-- Breads & Rice (401-410)
(401, 'Tandoori Roti', 'Breads_Rice', 20.00, '1 pc'),
(402, 'Butter Naan', 'Breads_Rice', 50.00, '1 pc'),
(403, 'Garlic Naan', 'Breads_Rice', 60.00, '1 pc'),
(404, 'Lachha Paratha', 'Breads_Rice', 45.00, '1 pc'),
(405, 'Steamed Rice', 'Breads_Rice', 100.00, '1 Bowl'),
(406, 'Jeera Rice', 'Breads_Rice', 120.00, '1 Bowl'),
(407, 'Veg Biryani', 'Breads_Rice', 220.00, '1/2 Kg'),
(408, 'Chicken Biryani', 'Breads_Rice', 300.00, '1 Kg (Serves 2)'),
(409, 'Mutton Biryani', 'Breads_Rice', 400.00, '1 Kg (Serves 2)'),
(410, 'Peas Pulao', 'Breads_Rice', 150.00, '1 Bowl'),

-- Desserts & Sweets (501-508)
(501, 'Gulab Jamun', 'Sweets', 80.00, '2 pcs'),
(502, 'Rasgulla', 'Sweets', 70.00, '2 pcs'),
(503, 'Gajar Ka Halwa', 'Sweets', 120.00, '1 Plate'),
(504, 'Rasmalai', 'Sweets', 100.00, '2 pcs'),
(505, 'Vanilla Ice Cream', 'Desserts', 90.00, '2 Scoops'),
(506, 'Chocolate Brownie with Ice Cream', 'Desserts', 180.00, '1 Plate'),
(507, 'Kulfi', 'Sweets', 60.00, '1 Stick'),
(508, 'Fruit Salad', 'Desserts', 110.00, '1 Bowl'),

-- Beverages (601-610)
(601, 'Masala Chai', 'Beverages', 30.00, '1 Cup'),
(602, 'Filter Coffee', 'Beverages', 40.00, '1 Cup'),
(603, 'Sweet Lassi', 'Beverages', 60.00, '1 Glass'),
(604, 'Salted Lassi', 'Beverages', 60.00, '1 Glass'),
(605, 'Fresh Lime Soda', 'Beverages', 50.00, '1 Glass'),
(606, 'Cold Coffee', 'Beverages', 100.00, '1 Glass'),
(607, 'Mango Shake', 'Beverages', 120.00, '1 Glass'),
(608, 'Bottled Water', 'Beverages', 20.00, '1 Liter'),
(609, 'Coke', 'Beverages', 40.00, '300 ml'),
(610, 'Sprite', 'Beverages', 40.00, '300 ml');
