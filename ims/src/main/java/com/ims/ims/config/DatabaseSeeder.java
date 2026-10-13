package com.ims.ims.config;

import com.ims.ims.model.*;
import com.ims.ims.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Initializes baseline seed data and default administrator if missing.
 * Matches the reference layout and metrics for an immediate, populated dashboard experience.
 */
@Component
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Override
    public void run(String... args) {
        seedAdminUser();
        seedBaselineData();
    }

    private void seedAdminUser() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("123456");
            admin.setFullName("Admin User");
            admin.setEmail("admin@imspro.com");
            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            userRepository.save(admin);
            System.out.println("✅ Default Admin account created: admin / 123456");
        }
    }

    private void seedBaselineData() {
        if (activityLogRepository.count() == 0) {
            System.out.println("🌱 Initializing baseline demo data matching target dashboard layout...");

            // 1. Category: laptop
            Category category = categoryRepository.findByName("laptop").orElseGet(() -> {
                Category cat = new Category();
                cat.setName("laptop");
                return categoryRepository.save(cat);
            });

            // 2. Supplier: hawawi
            Supplier supplier = supplierRepository.findByName("hawawi").orElseGet(() -> {
                Supplier sup = new Supplier();
                sup.setName("hawawi");
                sup.setPhone("+966500000000");
                sup.setEmail("contact@hawawi.com");
                sup.setAddress("Technology District");
                return supplierRepository.save(sup);
            });

            // 3. Product: Producto #1 (Stock: 1)
            Product product = productRepository.findBySku("LAP-001").orElseGet(() -> {
                Product p = new Product();
                p.setName("Producto #1");
                p.setSku("LAP-001");
                p.setDescription("High performance laptop");
                p.setCategory(category);
                p.setSupplier(supplier);
                p.setCostPrice(new BigDecimal("500.00"));
                p.setSellPrice(new BigDecimal("800.00"));
                p.setStockQuantity(1);
                return productRepository.save(p);
            });

            // 4. Order: #7b23395a-a4d8-41d3-81cc-3dcff8166f18
            String sampleOrderNum = "7b23395a-a4d8-41d3-81cc-3dcff8166f18";
            if (orderRepository.findByOrderNumber(sampleOrderNum).isEmpty()) {
                Order order = new Order();
                order.setOrderNumber(sampleOrderNum);
                order.setOrderDate(LocalDateTime.now().minusDays(2));
                order.setSupplier(supplier);

                OrderItem item = new OrderItem(1, new BigDecimal("500.00"), order, product);
                order.addItem(item);
                orderRepository.save(order);
            }

            // 5. Activity Logs (Timed > 24 hours ago to show "More than 24 hours ago")
            LocalDateTime logTime = LocalDateTime.now().minusDays(2);

            ActivityLog log4 = new ActivityLog("CATEGORY", "New Category Created", "Category #1 with the name of laptop", logTime.minusMinutes(15));
            ActivityLog log3 = new ActivityLog("SUPPLIER", "New Supplier Created", "Supplier #1 with the name of hawawi", logTime.minusMinutes(10));
            ActivityLog log2 = new ActivityLog("PRODUCT", "New Product Created", "Product #1 for Supplier hawawi", logTime.minusMinutes(5));
            ActivityLog log1 = new ActivityLog("ORDER", "New Order Created", "Order #7b23395a-a4d8-41d3-81cc-3dcff8166f18 for Supplier hawawi", logTime);

            activityLogRepository.save(log4);
            activityLogRepository.save(log3);
            activityLogRepository.save(log2);
            activityLogRepository.save(log1);

            System.out.println("✅ Baseline demo data initialized successfully!");
        }
    }
}
