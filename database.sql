-- ============================================================
-- Campus Lost & Found Manager - database setup script
-- Run this once in MySQL to create the database, the table
-- and some sample records for testing.
-- WARNING: it drops and recreates the items table, so any
-- existing data in it is deleted.
-- ============================================================

-- 1. Create the database (only if it does not exist yet)
CREATE DATABASE IF NOT EXISTS lostfound_db;

-- 2. Switch to that database so the commands below run inside it
USE lostfound_db;

-- 3. Start fresh: remove the old table if it exists
DROP TABLE IF EXISTS items;

-- 4. Create the table that stores every reported item
CREATE TABLE items (
    item_id     INT AUTO_INCREMENT PRIMARY KEY,   -- unique number, MySQL generates it
    item_name   VARCHAR(100) NOT NULL,            -- e.g. 'Black Wallet'
    category    VARCHAR(50)  NOT NULL,            -- e.g. 'Electronics'
    location    VARCHAR(100) NOT NULL,            -- where it was lost or found
    description VARCHAR(255),                     -- optional extra details
    status      VARCHAR(10)  NOT NULL,            -- 'Lost', 'Found' or 'Returned'
    reported_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- filled in automatically
    CONSTRAINT chk_status CHECK (status IN ('Lost', 'Found', 'Returned'))
);

-- 5. Sample records for testing and demonstration
INSERT INTO items (item_name, category, location, description, status) VALUES
('Black Wallet',      'Accessories', 'Library 2nd Floor', 'Leather wallet with student ID inside', 'Lost'),
('Casio Calculator',  'Electronics', 'Room B-104',        'fx-991EX, name scratched on back',      'Found'),
('Blue Water Bottle', 'Other',       'Sports Complex',    'Steel bottle with stickers',            'Found'),
('Java Textbook',     'Books',       'Cafeteria',         'Head First Java, 2nd edition',          'Lost'),
('Hostel Room Key',   'Keys',        'Main Gate',         'Single key on a red keyring',           'Returned');

-- 6. Check the result
SELECT * FROM items;
