# IMS PRO - System Specifications (Spec-Kit)

Welcome to the official system specifications file for **IMS PRO**. This document acts as the single source of truth for the project's technical architecture, features, and constraints.

---

## 1. System Overview
* **System Name:** IMS PRO (Inventory Management System Professional)
* **Goal:** A multi-role web-based system to manage inventory levels, suppliers, categories, users, and audit trail transactions, complete with a financial dashboard.
* **Roles supported:** 
  * `ADMIN`: Full access to CRUD operations, user administration, and financial metrics.
  * `STAFF`: Restricted access. Read-only inventory query, create transactions (Stock In/Out/Return), and view personal transaction history.

---

## 2. Technical Stack
* **Language:** Java 17
* **Framework:** Spring Boot 3.5.6
* **ORM / Database Driver:** Hibernate / Spring Data JPA + MySQL Connector
* **Database:** MySQL (local connection default port 3306)
* **Template Engine:** Thymeleaf 3.5.6 (HTML5 + dynamic attributes)
* **CSS & Frontend Components:** Bootstrap 5.1.3 + Font Awesome 6 (CDNs)
* **Charting Libraries:** ApexCharts (3.45.0) + Chart.js (3.9.1)

---

## 3. Database Schema & Data Models

### 3.1 `users` Table
Holds credential and role data for authorization.
* `user_id` (Long, PK, Auto-Increment)
* `username` (String, Unique, Not Null)
* `password` (String, Not Null) - *Note: Currently plain text (Security Hardening pending).*
* `full_name` (String, Not Null)
* `email` (String, Unique)
* `role` (Enum: `ADMIN`, `STAFF`, Not Null)
* `active` (Boolean, Default: true)

### 3.2 `categories` Table
Used to organize products.
* `category_id` (Long, PK, Auto-Increment)
* `name` (String, Not Null)
* Relations: One-To-Many with `products`.

### 3.3 `suppliers` Table
Stores details of vendors supplying inventory.
* `supplier_id` (Long, PK, Auto-Increment)
* `name` (String, Not Null)
* `contact_info` (String)
* `phone` (String)
* `email` (String)
* `address` (Text)
* Relations: One-To-Many with `products`.

### 3.4 `products` Table
Represents stock items. Soft delete enabled.
* `product_id` (Long, PK, Auto-Increment)
* `sku` (String, Unique, Not Null)
* `name` (String, Not Null)
* `description` (Text)
* `cost_price` (Decimal(10,2))
* `sell_price` (Decimal(10,2))
* `stock_quantity` (Integer, Default: 0)
* `deleted` (Boolean, Default: false)
* `created_at` (LocalDateTime)
* `updated_at` (LocalDateTime)

### 3.5 `transactions` Table
Logs all stock changes (Audit Trail).
* `transaction_id` (Long, PK, Auto-Increment)
* `product_id` (Long, FK, Not Null)
* `user_id` (Long, FK, Not Null)
* `transaction_type` (Enum: `SALE`, `PURCHASE`, `ADDITION`, `WITHDRAWAL`, `RETURN`, `ADJUSTMENT`, Not Null)
* `quantity` (Integer, Min: 1, Not Null)
* `previous_quantity` (Integer) - *For auditing/historical tracking*
* `new_quantity` (Integer) - *For auditing/historical tracking*
* `transaction_date` (LocalDateTime, Default: now)
* `notes` (Text)

---

## 4. Key Logic & Rules

### 4.1 Stock Changes & Transactions
Every saved transaction updates the product's `stock_quantity` field according to the transaction type:
* `SALE`, `WITHDRAWAL`: `stock_quantity = current_stock - quantity`. **Validation:** Throws `IllegalArgumentException` if `current_stock < quantity`.
* `PURCHASE`, `ADDITION`, `RETURN`: `stock_quantity = current_stock + quantity`.
* `ADJUSTMENT`: `stock_quantity = quantity` (overwrites stock to exact number).

### 4.2 Soft Delete Pattern
* Products are decorated with Hibernate `@SQLDelete(sql = "UPDATE products SET deleted = true WHERE product_id=?")` and `@Where(clause = "deleted=false")`.
* A delete request does NOT remove the DB row; it flags `deleted=true` so historical logs pointing to the `product_id` remain intact without breaking constraints.

### 4.3 Financial Dashboard Formulas
* **Gross Profit:** `totalRevenue - totalCosts`
* **Total Tax (10%):** `totalRevenue * 0.10`
* **Net Profit:** `Gross Profit - Total Tax`
* **Profit Margin (%):** `(Net Profit / Total Revenue) * 100`
* **Inventory Value:** `Σ(stockQuantity * costPrice)`

---

## 5. Security & Session Rules
* `HttpSession` holds logged-in state. Keys: `"user"` (Object), `"username"` (String), `"role"` (String).
* Handled via `isAdmin(session)` verification check.
* Any admin request (`/admin/**`) must redirect to `/login` if `isAdmin` returns false.

---

## 6. Project Guidelines for Updates
1. **Spec-first:** Any changes must have a corresponding file under `specs/features/` approved by the product owner before implementation.
2. **Backward Compatibility:** Database schema updates must use JPA annotations compatible with `spring.jpa.hibernate.ddl-auto=update`.
3. **Circular Dependencies:** Do not pass entities with bidirectional relationships directly to Thymeleaf JSON scripts (always use flat maps or DTOs).
