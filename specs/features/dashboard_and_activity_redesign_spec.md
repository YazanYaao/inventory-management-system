# SPEC-KIT: Feature Specification - Order System & Dashboard Redesign

**Feature ID:** `SPEC-DASH-002`  
**Status:** `CONFIRMED / APPROVED AFTER CLARIFICATION`  
**Author:** AntiGravity Agent  
**Date:** 2026-10-05  
**Target Module:** `ims` (Spring Boot + Thymeleaf)  
**Reference Source:** `inventory-management-API` & `inventory-management-front`  

---

## 1. Summary of Decisions from Clarification (`spec-kit.clarify`)
Following the clarification session with the product owner:
1. **Remove Sale Option & Adopt Supplier Orders:** The retail "Sale" transaction concept is removed. The system adopts the reference project's B2B / Supplier Order architecture (`Order` + `OrderItem`). Stock is replenished through Orders placed with Suppliers.
2. **Monthly Orders Calculation:** "Monthly Orders" metric and the "Orders by Month" chart are calculated directly from the `Order` entity (orders placed within the current month, and monthly order counts over the last 5–6 months).
3. **Activity Log Language:** Formatted in clear, professional English:
   - `"New Order Created"`: `Order #{orderNumber} for Supplier {supplierName}`
   - `"New Product Created"`: `Product #{id} for Supplier {supplierName}`
   - `"New Supplier Created"`: `Supplier #{id} with the name of {supplierName}`
   - `"New Category Created"`: `Category #{id} with the name of {categoryName}`
4. **Low Stock Threshold:** Exactly 5 units (matching `useProductsWithLessStockThan(5)` from the reference project).
   - If count == 0: Status shows `Stock in order`
   - If count > 0: Status shows `Requires attention` (in red badge)
5. **Dashboard Purge:** Remove legacy financial metrics (Revenue, COGS, Tax, Profit Margin, etc.) entirely from the dashboard.
6. **Design Continuity:** The structure matches the reference image (4 cards + 2 charts + Recent Activity) while keeping IMS PRO's visual style (Bootstrap 5, sleek cards, soft shadows, gradient accents, and responsive layout).

---

## 2. Architecture & Data Model Additions

### 2.1 Entity: `Order`
Represents purchase orders placed with suppliers:
* `id` (`Long`, PK, Generated)
* `orderNumber` (`String`, Unique, UUID or readable code)
* `orderDate` (`LocalDateTime`, not null)
* `supplier` (`@ManyToOne` to `Supplier`)
* `items` (`@OneToMany` to `OrderItem`, CascadeType.ALL, orphanRemoval = true)

### 2.2 Entity: `OrderItem`
Line items in an order:
* `id` (`Long`, PK, Generated)
* `quantity` (`int`, min 1)
* `price` (`BigDecimal`)
* `order` (`@ManyToOne` to `Order`)
* `product` (`@ManyToOne` to `Product`)

### 2.3 Entity: `ActivityLog`
System audit event log:
* `id` (`Long`, PK, Generated)
* `type` (`String`: "ORDER", "PRODUCT", "SUPPLIER", "CATEGORY")
* `title` (`String`)
* `description` (`String`, Text)
* `createdAt` (`LocalDateTime`)

---

## 3. Backend Logic & Services

### 3.1 `OrderService` & `OrderRepository`
1. `createOrder(Long supplierId, List<OrderItemDTO> items)`:
   - Generates unique `orderNumber`.
   - Associates items with order and product.
   - Automatically increases product `stockQuantity` (Stock IN).
   - Saves order and creates `ActivityLog` ("New Order Created").
2. `countOrdersThisMonth()`:
   - Returns count of orders where `orderDate` is within current `YearMonth`.
3. `getOrderCountLastMonths(int months)`:
   - Aggregates order count grouped by Year-Month for the last 5–6 months (e.g., June -> October) returning `List<Map<String, Object>>` with `name` and `value`.

### 3.2 `ActivityLogService` & `ActivityLogRepository`
1. `logActivity(String type, String title, String description)`
2. `getRecentActivities()`:
   - Fetches top 5 by `createdAt` descending.
   - Formats relative time:
     - `< 1 hour`: "Just now"
     - `> 24 hours`: "More than 24 hours ago"
     - else: `X hours ago`

### 3.3 Dashboard Controller Data Injection
In `AdminController.showDashboard()`:
* `totalProducts`: `productRepository.count()` (excluding deleted).
* `lowStockCount`: count where `stockQuantity <= 5`.
* `lowStockStatus`: "Stock in order" if 0, else "Requires attention".
* `monthlyOrdersCount`: count from `orderService.countOrdersThisMonth()`.
* `activeSuppliersCount`: `supplierRepository.count()`.
* `monthlyOrdersChartData`: list of past 5 months with order counts.
* `categoryDistributionChartData`: list of categories with their total product stock quantity.
* `recentActivities`: list of top 5 activity logs.

---

## 4. UI Layout (`admin/dashboard.html`)

```
+-----------------------------------------------------------------------------------------+
|                                    📈 Dashboard                                         |
+----------------------+--------------------+--------------------+------------------------+
| Total Products       | Low Stock          | Monthly Orders     | Active Suppliers       |
| 1                    | 0                  | 1                  | 1                      |
| 📈 +2.5% from month  | Stock in order     | 📈 +5% from yest.  | No changes from month  |
+----------------------+--------------------+--------------------+------------------------+
|                                                                                         |
| +-----------------------------------------+ +-----------------------------------------+ |
| | Orders by Month                         | | Distribution by Category                | |
| | Monthly order analysis                  | | Products by category                    | |
| | [ Line Chart: Jun -> Oct ]              | | [ Bar Chart: Purple, Category Stock ]   | |
| +-----------------------------------------+ +-----------------------------------------+ |
|                                                                                         |
| +-------------------------------------------------------------------------------------+ |
| | Recent Activity                                                                     | |
| | Latest system updates                                                               | |
| | - Nueva/New Order Created       Order #... for Supplier hawawi  More than 24 hrs ago| |
| | - Nueva/New Product Created     Product #1 for Supplier hawawi  More than 24 hrs ago| |
| | - Nueva/New Supplier Created    Supplier #1 with name hawawi    More than 24 hrs ago| |
| | - Nueva/New Category Created    Category #1 with name laptop    More than 24 hrs ago| |
| +-------------------------------------------------------------------------------------+ |
+-----------------------------------------------------------------------------------------+
```

---

## 5. Execution Plan
1. **Model Layer**: Create `ActivityLog`, `Order`, and `OrderItem`.
2. **Repository Layer**: Create `ActivityLogRepository` and `OrderRepository`.
3. **Service Layer**: Create `ActivityLogService`, `OrderService`, and update `CategoryService`, `ProductService`, `SupplierService` to trigger activity logs upon creation.
4. **Controller Layer**:
   - Update `AdminController.showDashboard()` to supply all required data models.
   - Add Order management endpoints (list orders, create order).
5. **View Layer**:
   - Overhaul `admin/dashboard.html` with the 4 cards, 2 charts (ApexCharts/Chart.js), and Recent Activity feed.
   - Update navigation menu: replace "Transactions" with "Orders".
   - Create Order Management views if needed (`admin/orders.html`, `admin/add-order.html`).
6. **Compilation & Testing**:
   - Run `.\mvnw.cmd compile` and ensure zero errors.
   - Seed sample activity logs / test data if DB is empty so the dashboard immediately renders like the reference screenshot.
