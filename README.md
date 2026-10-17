# Sales and Customer Management System

A Java desktop application for managing customers, products, inventory, orders and payments using MySQL. It combines a graphical interface with database operations, transaction handling and sales reporting.

## Features

- **Dashboard** — Overview of customers, products, orders and payments.
- **Master data management** — CRUD operations for categories, suppliers, products, customers and employees.
- **Order processing** — Order creation, stock validation and inventory updates.
- **Payment management** — View payment records associated with orders.
- **Reports** — Monthly sales, customer spending, best-selling products, low-stock items and payment reconciliation.
- **Authentication and roles** — Password hashing, temporary login lockout and separate administrator and staff permissions.

## Tech Stack

- Java and Java Swing
- MySQL and JDBC
- MySQL Connector/J
- `javac` and Bash scripts

## Database Design

The `sales_customer_db` database contains eight main business tables:

| Table | Purpose |
|---|---|
| `Category` | Product categories |
| `Supplier` | Supplier details |
| `Product` | Product information, prices and stock |
| `Customer` | Customer records |
| `Employee` | Employee records |
| `Orders` | Customer orders |
| `Order_Details` | Products and quantities in each order |
| `Payment` | Payment records |

A separate `Users` table handles authentication and roles. Primary and foreign keys maintain relationships between records.

## Getting Started

### Prerequisites

- JDK 17 or later
- MySQL Server
- MySQL Connector/J

### 1. Clone the repository

```bash
git clone https://github.com/princehirwani2110/sales-customer-management-system.git
cd sales-customer-management-system
```

### 2. Set up the database

Run `sales_customer_db.sql` in MySQL, followed by `02_security_extension.sql`.

**Note:** The main SQL script drops and recreates the database. Run it only during initial setup unless you intend to erase existing data.

### 3. Configure the connection

Copy the example configuration:

```bash
cp config/db.properties.example config/db.properties
```

Update `config/db.properties` with your local database credentials. Use the restricted `sales_app` account rather than MySQL `root`. Do not commit your local configuration file.

### 4. Compile and run

```bash
bash scripts/compile.sh
bash scripts/setup-admin.sh
bash scripts/run-login.sh
```

Create an administrator account when prompted, then use it to log in.

## Future Scope

- Multiple products per order, discounts, taxes and partial payments
- PDF and Excel report exports with filters and charts
- Audit logs and password-reset functionality
- A server-side REST API for database access
- Automated integration tests and improved concurrent stock handling

## License

No license has been added yet.
