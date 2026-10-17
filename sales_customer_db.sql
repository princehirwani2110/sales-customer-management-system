-- ============================================================================
-- SALES AND CUSTOMER MANAGEMENT DATABASE SYSTEM
-- BACSE202 - Lab Component Project - Review 0
-- Prince Hirwani | 25BDS0264
-- Run this file top to bottom in MySQL Workbench (or `mysql -u root -p < sales_customer_db.sql`)
-- ============================================================================

DROP DATABASE IF EXISTS sales_customer_db;
CREATE DATABASE sales_customer_db;
USE sales_customer_db;

-- ============================================================================
-- 1. TABLE CREATION WITH ALL CONSTRAINTS
-- ============================================================================

CREATE TABLE Category (
    category_id     INT PRIMARY KEY AUTO_INCREMENT,
    category_name   VARCHAR(50) NOT NULL UNIQUE,
    description     VARCHAR(150)
);

CREATE TABLE Supplier (
    supplier_id     INT PRIMARY KEY AUTO_INCREMENT,
    supplier_name   VARCHAR(50) NOT NULL,
    contact_number  VARCHAR(15) NOT NULL,
    email           VARCHAR(50) UNIQUE,
    address         VARCHAR(100)
);

CREATE TABLE Product (
    product_id      INT PRIMARY KEY AUTO_INCREMENT,
    product_name    VARCHAR(50) NOT NULL,
    category_id     INT,
    supplier_id     INT,
    price           DECIMAL(10,2) NOT NULL CHECK (price > 0),
    stock_quantity  INT DEFAULT 0 CHECK (stock_quantity >= 0),
    FOREIGN KEY (category_id) REFERENCES Category(category_id) ON DELETE SET NULL,
    FOREIGN KEY (supplier_id) REFERENCES Supplier(supplier_id) ON DELETE SET NULL
);

CREATE TABLE Customer (
    customer_id     INT PRIMARY KEY AUTO_INCREMENT,
    customer_name   VARCHAR(50) NOT NULL,
    phone           VARCHAR(15) NOT NULL UNIQUE,
    email           VARCHAR(50) UNIQUE,
    city            VARCHAR(30),
    join_date       DATE DEFAULT (CURRENT_DATE)
);

CREATE TABLE Employee (
    employee_id     INT PRIMARY KEY AUTO_INCREMENT,
    employee_name   VARCHAR(50) NOT NULL,
    designation     VARCHAR(30),
    phone           VARCHAR(15) NOT NULL,
    salary          DECIMAL(10,2) CHECK (salary > 0)
);

CREATE TABLE Orders (
    order_id        INT PRIMARY KEY AUTO_INCREMENT,
    customer_id     INT NOT NULL,
    employee_id     INT,
    order_date      DATE DEFAULT (CURRENT_DATE),
    order_status    VARCHAR(20) DEFAULT 'Pending',
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id) ON DELETE CASCADE,
    FOREIGN KEY (employee_id) REFERENCES Employee(employee_id) ON DELETE SET NULL
);

CREATE TABLE Order_Details (
    order_detail_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id        INT NOT NULL,
    product_id      INT NOT NULL,
    quantity        INT NOT NULL CHECK (quantity > 0),
    unit_price      DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES Product(product_id)
);

CREATE TABLE Payment (
    payment_id      INT PRIMARY KEY AUTO_INCREMENT,
    order_id        INT NOT NULL UNIQUE,
    payment_date    DATE DEFAULT (CURRENT_DATE),
    amount          DECIMAL(10,2) NOT NULL,
    payment_mode    VARCHAR(20) CHECK (payment_mode IN ('Cash','Card','UPI','Net Banking')),
    FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON DELETE CASCADE
);


-- ============================================================================
-- 2. INSERTION (up to 5 rows per table)
-- ============================================================================

INSERT INTO Category (category_name, description) VALUES
('Electronics', 'Gadgets and electronic devices'),
('Clothing',    'Apparel for men and women'),
('Groceries',   'Daily use food items'),
('Furniture',   'Home and office furniture'),
('Stationery',  'Office and school supplies');

INSERT INTO Supplier (supplier_name, contact_number, email, address) VALUES
('Raj Traders',     '9876543210', 'raj.traders@mail.com',  'Chennai'),
('Sri Electronics',  '9123456780', 'srielec@mail.com',      'Bangalore'),
('Fashion Hub',      '9988776655', 'fashionhub@mail.com',   'Coimbatore'),
('Home Comforts',    '9871234560', 'homecomforts@mail.com', 'Vellore'),
('Paper World',      '9765432109', 'paperworld@mail.com',   'Chennai');

INSERT INTO Product (product_name, category_id, supplier_id, price, stock_quantity) VALUES
('Bluetooth Earphones', 1, 2, 1299.00, 50),
('Men Cotton Shirt',    2, 3,  799.00, 100),
('Basmati Rice 5kg',    3, 1,  450.00, 200),
('Study Table',         4, 4, 3500.00, 20),
('Notebook Pack',       5, 5,  150.00, 300);

INSERT INTO Customer (customer_name, phone, email, city, join_date) VALUES
('Arjun Kumar',  '9000011111', 'arjun.k@mail.com',   'Vellore',     '2025-01-15'),
('Priya Sharma', '9000022222', 'priya.s@mail.com',   'Chennai',     '2025-02-20'),
('Karthik Raj',  '9000033333', 'karthik.r@mail.com', 'Bangalore',   '2025-03-05'),
('Sneha Reddy',  '9000044444', 'sneha.r@mail.com',   'Hyderabad',   '2025-04-11'),
('Vishal Menon', '9000055555', 'vishal.m@mail.com',  'Coimbatore',  '2025-05-19');

INSERT INTO Employee (employee_name, designation, phone, salary) VALUES
('Ramesh Iyer', 'Sales Executive', '9111122223', 25000.00),
('Divya Nair',  'Sales Executive', '9111133334', 26000.00),
('Suresh Babu', 'Store Manager',   '9111144445', 40000.00),
('Anitha Rao',  'Cashier',         '9111155556', 20000.00),
('Manoj Pillai','Sales Executive', '9111166667', 25500.00);

INSERT INTO Orders (customer_id, employee_id, order_date, order_status) VALUES
(1, 1, '2026-01-10', 'Delivered'),
(2, 2, '2026-01-12', 'Delivered'),
(3, 1, '2026-01-15', 'Pending'),
(4, 3, '2026-01-18', 'Delivered'),
(5, 2, '2026-01-20', 'Cancelled');

INSERT INTO Order_Details (order_id, product_id, quantity, unit_price) VALUES
(1, 1, 2, 1299.00),
(2, 3, 1,  450.00),
(3, 4, 1, 3500.00),
(4, 2, 3,  799.00),
(5, 5, 5,  150.00);

INSERT INTO Payment (order_id, payment_date, amount, payment_mode) VALUES
(1, '2026-01-10', 2598.00, 'UPI'),
(2, '2026-01-12',  450.00, 'Cash'),
(4, '2026-01-18', 2397.00, 'Card'),
(3, '2026-01-15', 3500.00, 'UPI');


-- ============================================================================
-- 3. DELETION
-- ============================================================================

DELETE FROM Orders WHERE order_status = 'Cancelled';
-- ON DELETE CASCADE on Order_Details and Payment removes any related child rows.


-- ============================================================================
-- 4. UPDATION (on each table)
-- ============================================================================

UPDATE Customer      SET city = 'Bangalore'                          WHERE customer_id = 4;
UPDATE Employee      SET salary = 27000.00                           WHERE employee_id = 1;
UPDATE Product       SET stock_quantity = stock_quantity - 2         WHERE product_id = 1;
UPDATE Category      SET description = 'Electronic gadgets and accessories' WHERE category_id = 1;
UPDATE Supplier      SET contact_number = '9876500000'               WHERE supplier_id = 1;
UPDATE Orders        SET order_status = 'Delivered'                  WHERE order_id = 3;
UPDATE Order_Details SET quantity = 4                                WHERE order_detail_id = 4;
UPDATE Payment       SET payment_mode = 'Net Banking'                WHERE payment_id = 2;


-- ============================================================================
-- 5. SELECT
-- ============================================================================

SELECT * FROM Customer;
SELECT product_name, price, stock_quantity FROM Product;


-- ============================================================================
-- 6. SINGLE ROW FUNCTIONS
-- ============================================================================

SELECT customer_name, UPPER(city) AS city_upper, LENGTH(customer_name) AS name_length
FROM Customer;

SELECT product_name, ROUND(price, 0) AS rounded_price
FROM Product;

SELECT order_id, order_date, MONTHNAME(order_date) AS order_month
FROM Orders;


-- ============================================================================
-- 7. SQL FUNCTIONS (AGGREGATE)
-- ============================================================================

SELECT COUNT(*) AS total_customers FROM Customer;
SELECT AVG(price) AS average_price FROM Product;
SELECT MAX(salary) AS highest_salary, MIN(salary) AS lowest_salary FROM Employee;
SELECT SUM(quantity * unit_price) AS total_sales FROM Order_Details;


-- ============================================================================
-- 8. GROUP BY
-- ============================================================================

SELECT category_id, COUNT(*) AS total_products
FROM Product
GROUP BY category_id;


-- ============================================================================
-- 9. HAVING CLAUSE
-- ============================================================================

SELECT c.customer_id, c.customer_name, COUNT(o.order_id) AS total_orders
FROM Customer c
JOIN Orders o ON c.customer_id = o.customer_id
GROUP BY c.customer_id, c.customer_name
HAVING COUNT(o.order_id) >= 1;


-- ============================================================================
-- 10. SUB QUERY
-- ============================================================================

SELECT product_name, price
FROM Product
WHERE price > (SELECT AVG(price) FROM Product);


-- ============================================================================
-- 11. JOIN QUERY
-- ============================================================================

SELECT o.order_id, c.customer_name, p.product_name, od.quantity, od.unit_price
FROM Orders o
JOIN Customer c ON o.customer_id = c.customer_id
JOIN Order_Details od ON o.order_id = od.order_id
JOIN Product p ON od.product_id = p.product_id;
