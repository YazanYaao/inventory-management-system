package com.ims.ims.service;

import com.ims.ims.exception.ResourceNotFoundException;
import com.ims.ims.model.Order;
import com.ims.ims.model.OrderItem;
import com.ims.ims.model.Product;
import com.ims.ims.model.Supplier;
import com.ims.ims.repository.OrderRepository;
import com.ims.ims.repository.ProductRepository;
import com.ims.ims.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }

    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
    }

    /**
     * Creates a new purchase order with a supplier and receives stock into the inventory
     */
    public Order createOrder(Long supplierId, List<Long> productIds, List<Integer> quantities, List<BigDecimal> prices) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + supplierId));

        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setOrderDate(LocalDateTime.now());
        order.setSupplier(supplier);

        if (productIds != null && quantities != null) {
            for (int i = 0; i < productIds.size(); i++) {
                Long productId = productIds.get(i);
                int qty = quantities.get(i);
                if (qty <= 0) continue;

                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

                BigDecimal itemPrice = (prices != null && i < prices.size() && prices.get(i) != null)
                        ? prices.get(i)
                        : (product.getCostPrice() != null ? product.getCostPrice() : BigDecimal.ZERO);

                OrderItem item = new OrderItem(qty, itemPrice, order, product);
                order.addItem(item);

                // Replenish stock quantity (Stock In)
                int currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
                product.setStockQuantity(currentStock + qty);
                productRepository.save(product);
            }
        }

        Order savedOrder = orderRepository.save(order);

        // Record Activity Log
        activityLogService.logActivity(
                "ORDER",
                "New Order Created",
                "Order #" + savedOrder.getOrderNumber() + " for Supplier " + supplier.getName()
        );

        return savedOrder;
    }

    /**
     * Counts orders placed during the current calendar month
     */
    @Transactional(readOnly = true)
    public long countOrdersThisMonth() {
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = LocalDateTime.now();
        return orderRepository.countByOrderDateBetween(startOfMonth, endOfMonth);
    }

    /**
     * Aggregates order count for the last N months (e.g. 5 months: June, July, August, September, October).
     * Missing months default to 0 to ensure smooth line chart rendering.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getOrderCountLastMonths(int months) {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime startDate = LocalDate.now().minusMonths(months - 1).withDayOfMonth(1).atStartOfDay();

        List<Object[]> results = orderRepository.countOrdersGroupedByMonth(startDate, today);

        Map<YearMonth, Long> counts = results.stream()
                .collect(Collectors.toMap(
                        r -> YearMonth.of(((Number) r[0]).intValue(), ((Number) r[1]).intValue()),
                        r -> ((Number) r[2]).longValue(),
                        (a, b) -> a
                ));

        List<Map<String, Object>> monthlyCounts = new ArrayList<>();
        for (int i = months - 1; i >= 0; i--) {
            YearMonth ym = YearMonth.from(today.minusMonths(i));
            String monthName = ym.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            Long count = counts.getOrDefault(ym, 0L);

            Map<String, Object> point = new HashMap<>();
            point.put("name", monthName);
            point.put("value", count);
            monthlyCounts.add(point);
        }

        return monthlyCounts;
    }

    public void deleteOrder(Long id) {
        Order order = getOrderById(id);
        orderRepository.delete(order);
    }

    private String generateOrderNumber() {
        return UUID.randomUUID().toString();
    }
}
