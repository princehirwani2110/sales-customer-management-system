-- AST09 security extension for Sales and Customer Management Database System
-- Run this only AFTER sales_customer_db.sql has created the eight business tables.
-- IMPORTANT: the original sales_customer_db.sql begins with DROP DATABASE. Do not rerun it
-- after this extension unless you intentionally want to erase all project data and Users.
-- Replace the sample password below with a strong, unique local password BEFORE running.
-- Do not commit/share the real password or this edited file.

USE sales_customer_db;

CREATE TABLE IF NOT EXISTS Users (
    user_id         INT PRIMARY KEY AUTO_INCREMENT,
    username        VARCHAR(50) NOT NULL UNIQUE,
    password_hash   VARCHAR(128) NOT NULL,
    password_salt   VARCHAR(128) NOT NULL,
    iterations      INT NOT NULL DEFAULT 210000,
    `role`          VARCHAR(10) NOT NULL,
    employee_id     INT NULL UNIQUE,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    failed_attempts INT NOT NULL DEFAULT 0,
    locked_until    DATETIME NULL,
    last_login      DATETIME NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_users_role CHECK (`role` IN ('ADMIN','STAFF')),
    CONSTRAINT chk_users_iterations CHECK (iterations >= 100000),
    CONSTRAINT chk_users_failed_attempts CHECK (failed_attempts >= 0),
    CONSTRAINT fk_users_employee FOREIGN KEY (employee_id)
        REFERENCES Employee(employee_id) ON DELETE SET NULL
);

-- This creates a local application account with table-scoped CRUD rights.
-- Replace the placeholder password before executing this statement.
-- SAFETY: these account statements are commented out to prevent accidentally creating
-- a known default password. Replace the placeholder with a strong password, then remove
-- the leading '-- ' from each of the following statements before executing this file.
-- CREATE USER IF NOT EXISTS 'sales_app'@'localhost' IDENTIFIED BY 'REPLACE_WITH_A_STRONG_UNIQUE_PASSWORD';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON sales_customer_db.Category TO 'sales_app'@'localhost';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON sales_customer_db.Supplier TO 'sales_app'@'localhost';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON sales_customer_db.Product TO 'sales_app'@'localhost';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON sales_customer_db.Customer TO 'sales_app'@'localhost';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON sales_customer_db.Employee TO 'sales_app'@'localhost';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON sales_customer_db.Orders TO 'sales_app'@'localhost';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON sales_customer_db.Order_Details TO 'sales_app'@'localhost';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON sales_customer_db.Payment TO 'sales_app'@'localhost';
-- GRANT SELECT, INSERT, UPDATE ON sales_customer_db.Users TO 'sales_app'@'localhost';

-- Verify manually as an administrator:
-- SHOW GRANTS FOR 'sales_app'@'localhost';
-- SELECT COUNT(*) FROM sales_customer_db.Users;
