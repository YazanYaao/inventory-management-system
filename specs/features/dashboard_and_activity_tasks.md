# SPEC-KIT: Task Breakdown - Order System & Dashboard Redesign

**Task Document ID:** `TASKS-DASH-001`  
**Plan Ref:** [`specs/features/dashboard_and_activity_plan.md`](./dashboard_and_activity_plan.md)  
**Spec Ref:** [`specs/features/dashboard_and_activity_redesign_spec.md`](./dashboard_and_activity_redesign_spec.md)  
**Status:** `READY TO EXECUTE`  

---

## Task Matrix & Dependency Graph

| Task ID | Component | Task Description | Dependencies | Status |
| :--- | :--- | :--- | :--- | :--- |
| **TASK-01** | Model | Create `ActivityLog.java` entity with relative time calculation | None | ✅ Completed |
| **TASK-02** | Model | Create `Order.java` entity model for supplier orders | None | ✅ Completed |
| **TASK-03** | Model | Create `OrderItem.java` entity model | TASK-02 | ✅ Completed |
| **TASK-04** | Repository | Create `ActivityLogRepository.java` (`findTop5ByOrderByCreatedAtDesc`) | TASK-01 | ✅ Completed |
| **TASK-05** | Repository | Create `OrderRepository.java` with monthly grouping query | TASK-02 | ✅ Completed |
| **TASK-06** | Service | Implement `ActivityLogService.java` with logging and formatting | TASK-04 | ✅ Completed |
| **TASK-07** | Service | Implement `OrderService.java` (create order, stock In, monthly aggregation) | TASK-05, TASK-06 | ✅ Completed |
| **TASK-08** | Service Hooks | Hook activity logging into `ProductService`, `CategoryService`, `SupplierService` | TASK-06 | ✅ Completed |
| **TASK-09** | Controller | Refactor `AdminController.showDashboard()` to inject 4 cards, 2 charts, and feed | TASK-06, TASK-07 | ✅ Completed |
| **TASK-10** | Controller | Add Order management routes (`/admin/orders`, `/admin/orders/add`, view) | TASK-07 | ✅ Completed |
| **TASK-11** | Template | Overhaul `dashboard.html` metric cards (Total Products, Low Stock, Orders, Suppliers) | TASK-09 | ✅ Completed |
| **TASK-12** | Template | Implement "Orders by Month" line chart & "Distribution by Category" bar chart | TASK-09 | ✅ Completed |
| **TASK-13** | Template | Implement "Recent Activity" feed component in `dashboard.html` | TASK-09 | ✅ Completed |
| **TASK-14** | Template | Update sidebar navigation (link to Orders instead of retail transactions) | TASK-10 | ✅ Completed |
| **TASK-15** | Template | Create `orders.html` and `add-order.html` templates for order management | TASK-10 | ✅ Completed |
| **TASK-16** | Seeding | Add sample activity logs & baseline orders if database is clean | TASK-06, TASK-07 | ✅ Completed |
| **TASK-17** | Verification | Run `mvnw compile` & verify zero errors and matching UI layout | All Tasks | ✅ Completed |

---

## Detailed Task Specifications

### [TASK-01] Create `ActivityLog.java`
- **File:** `ims/src/main/java/com/ims/ims/model/ActivityLog.java`
- **Fields:** `id`, `type`, `title`, `description`, `createdAt`
- **Method:** `getTimeAgo()`:
  - `< 1 hour`: `"Just now"`
  - `> 24 hours`: `"More than 24 hours ago"`
  - Else: `"X hours ago"`

### [TASK-02] Create `Order.java`
- **File:** `ims/src/main/java/com/ims/ims/model/Order.java`
- **Fields:** `id`, `orderNumber` (Unique UUID/code), `orderDate`, `supplier` (ManyToOne), `items` (OneToMany CascadeType.ALL, orphanRemoval=true)

### [TASK-03] Create `OrderItem.java`
- **File:** `ims/src/main/java/com/ims/ims/model/OrderItem.java`
- **Fields:** `id`, `quantity`, `price`, `order` (ManyToOne), `product` (ManyToOne)

### [TASK-04] Create `ActivityLogRepository.java`
- **File:** `ims/src/main/java/com/ims/ims/repository/ActivityLogRepository.java`
- **Method:** `List<ActivityLog> findTop5ByOrderByCreatedAtDesc()`

### [TASK-05] Create `OrderRepository.java`
- **File:** `ims/src/main/java/com/ims/ims/repository/OrderRepository.java`
- **Queries:**
  - `countOrdersGroupedByMonth(startDate, endDate)`
  - `long countByOrderDateBetween(startDate, endDate)`
  - `List<Order> findAllByOrderByOrderDateDesc()`

### [TASK-06] Implement `ActivityLogService.java`
- **File:** `ims/src/main/java/com/ims/ims/service/ActivityLogService.java`
- **Methods:**
  - `void logActivity(String type, String title, String description)`
  - `List<ActivityLog> getRecentActivities()`

### [TASK-07] Implement `OrderService.java`
- **File:** `ims/src/main/java/com/ims/ims/service/OrderService.java`
- **Methods:**
  - `Order createOrder(Long supplierId, List<Long> productIds, List<Integer> quantities)`
  - `long countOrdersThisMonth()`
  - `List<Map<String, Object>> getOrderCountLastMonths(int months)` (calculates exact month names and order counts for Chart.js/ApexCharts)

### [TASK-08] Hook Activity Logging into Domain Services
- Update `ProductService.java`: log `"New Product Created"` on creation.
- Update `CategoryService.java`: log `"New Category Created"` on creation.
- Update `SupplierService.java`: log `"New Supplier Created"` on creation.

### [TASK-09] Refactor `AdminController.showDashboard()`
- **Purge:** Remove legacy calculations (gross profit, net profit, tax, top products table).
- **Inject:**
  - `totalProducts`: count of non-deleted products
  - `lowStockCount`: count where `stockQuantity <= 5`
  - `lowStockStatus`: `"Stock in order"` if 0, else `"Requires attention"`
  - `monthlyOrdersCount`: count of orders this month
  - `activeSuppliersCount`: count of suppliers
  - `monthlyOrdersJson`: JSON array for line chart (X: Months, Y: Order quantity)
  - `categoryDistributionJson`: JSON array for bar chart (X: Category, Y: Stock quantity)
  - `recentActivities`: top 5 activity logs

### [TASK-10] Order Management Controller Routes
- `GET /admin/orders`: List all orders with supplier and item counts.
- `GET /admin/orders/add`: Form to select supplier, products, and quantities.
- `POST /admin/orders/add`: Process order creation and stock replenishment.

### [TASK-11] Overhaul Metric Cards in `dashboard.html`
- 4 clean white cards matching IMS PRO styles:
  1. **Total Products** | Value | `+2.5% from last month` (green)
  2. **Low Stock** | Value | `Stock in order` (or `Requires attention` in red)
  3. **Monthly Orders** | Value | `+5% from yesterday` (blue)
  4. **Active Suppliers** | Value | `No changes from last month` (gray)

### [TASK-12] Implement Charts in `dashboard.html`
- **Chart 1:** "Orders by Month" (Monthly order analysis) - Line chart with blue line `#3b82f6` and points.
- **Chart 2:** "Distribution by Category" (Products by category) - Bar chart with purple fill `#8B5CF6`.

### [TASK-13] Implement Recent Activity in `dashboard.html`
- Title: **Recent Activity** / Subtitle: *Latest system updates*.
- Row items showing Title, Description, and relative timestamp.

### [TASK-14] & [TASK-15] Navigation and Order Templates
- Update sidebar to link to `/admin/orders`.
- Create `admin/orders.html` and `admin/add-order.html`.

### [TASK-16] Baseline Data Seeder
- Ensure that if the database is initially empty, sample entries matching the reference image are present.

### [TASK-17] Verification & Compilation
- Run `.\mvnw.cmd compile` and ensure zero compilation errors.
