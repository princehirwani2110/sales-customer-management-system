# AST09 Stage 2 — Sales and Customer Management Database System

This is the Stage 2 foundation for the AST09 project. It is built for **Java Swing + JDBC + MySQL**, consistent with the existing Review-0 SQL and screenshots. The teacher's example uses Oracle, but the JDBC pattern is vendor-neutral; confirm with faculty that MySQL is acceptable before final submission.

## Included
- `sales_customer_db.sql`: original eight-table SQL script, copied unchanged from the supplied source.
- `02_security_extension.sql`: additive `Users` table and restricted `sales_app` DB account.
- Java 17-compatible source for configuration, JDBC connections, PBKDF2 password hashing, DAO, authentication/lockout, first-admin setup, and login screen.
- `scripts/compile.sh`, `scripts/setup-admin.sh`, and `scripts/run-login.sh`.
- `config/db.properties.example` only; your real `config/db.properties` is intentionally absent and ignored by Git.
- `assets/`: PDF-rendered pages and extracted images from Review-0, plus inventory/manifest.

## Setup on macOS
1. Install MySQL Server 8.0.16+ (8.0.46 was used in the previous external run reported in the pasted Stage 1 notes, but this current sandbox has no MySQL client/server available).
2. Open MySQL Workbench or Terminal. Run `sales_customer_db.sql` **only if you intend to create/reset the original demo database**. It begins with `DROP DATABASE IF EXISTS sales_customer_db` and will erase existing data in that database.
3. Set a strong unique password in `02_security_extension.sql` by replacing `REPLACE_WITH_A_STRONG_UNIQUE_PASSWORD`; execute the extension as a MySQL administrator after the original schema exists. Do not share the edited file with the password in it.
4. Obtain the official MySQL Connector/J JAR and place it at `lib/mysql-connector-j.jar`. The JAR is not included.
5. Copy `config/db.properties.example` to `config/db.properties`; set the same `sales_app` password. Never commit/share the real config.
6. Run `./scripts/compile.sh`.
7. Run `./scripts/setup-admin.sh` in a real terminal. The initial administrator password is read without echoing and stored only as a salted PBKDF2 hash.
8. Run `./scripts/run-login.sh`.

## Security notes
- No database password or admin password is hard-coded in Java source.
- The app account has CRUD rights on the eight business tables and SELECT/INSERT/UPDATE on `Users`; it has no schema-management privileges.
- Passwords use PBKDF2WithHmacSHA256 with a per-user random salt and 210,000 iterations; comparisons use `MessageDigest.isEqual`.
- Five consecutive failed attempts cause a 10-minute lockout. The message shown to the user is generic.
- This is a desktop app with database access configured on the client. Role restrictions inside the UI are not a security boundary against a user who can inspect client files; a server-side API is a future improvement.
- The SQL placeholder account password must be changed before executing the extension. The SQL file in this pack still contains the placeholder, not a real secret.

## Stage 2 verification status
- **Verified:** Java 21 is installed in the current sandbox; `javac --release 17` compilation succeeds; `pdftoppm`, `pdftotext`, `pdfimages`, and `xvfb-run` are available; the Review-0 ER page (PDF page 9) renders as a blank page under the heading `5. E-R DIAGRAM`.
- **Not verified here:** MySQL server/client, Connector/J availability, running SQL scripts, real DB login integration, and Swing screenshots connected to a database. This sandbox has no `mysql` or `mariadb` command and no Connector/J JAR was found.
- **Important:** The `sales_customer_db.sql` source is destructive at the top. Do not rerun it after setting up `Users` unless intentionally resetting everything.

## Stage 3 — dashboard and master-table CRUD
- Successful login now opens the authenticated dashboard; logout returns to the login screen.
- Dashboard shows customer/product/order counts, recorded payments excluding cancelled orders, recent orders, and low-stock products.
- Added reusable search/table/form screens for Categories, Suppliers, Products, Customers and Employees. Inserts, edits and deletes use `PreparedStatement`; input parsing and required fields are checked before submission.
- ADMIN can maintain all five master tables. STAFF can maintain Customers; other visible master tables are read-only, and Employees are hidden.
- Orders, Payments, Reports and User Management are clearly marked as Stage 4 placeholders. Do not describe them as implemented yet.
- Offline checks: run `./scripts/test-stage3.sh`. Database-backed UI actions require MySQL, `02_security_extension.sql`, `config/db.properties`, and Connector/J to be set up first.


## Stage 4 modules

The dashboard now includes Orders, Payments, Reports, and admin-only User Management. Orders are created transactionally for a single product line and record payment at creation. See `STAGE4_PROGRESS.md` for verified checks and known limitations. Live MySQL integration has not yet been verified in the build environment.
