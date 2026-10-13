package com.ims.ims.controller;

import com.ims.ims.model.Order;
import com.ims.ims.model.OrderRequestDTO;
import com.ims.ims.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Purchase Orders (API Layer)
 * Manages procurement workflows and stock replenishment from suppliers.
 */
@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order Management", description = "REST APIs for supplier purchase orders and stock replenishment")
public class OrderRestController {

    @Autowired
    private OrderService orderService;

    @Operation(summary = "Get all purchase orders", description = "Retrieves a list of all purchase orders sorted by order date descending.")
    @ApiResponse(responseCode = "200", description = "List of orders retrieved successfully")
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @Operation(summary = "Get order by ID", description = "Retrieves full details of a specific purchase order including line items.")
    @ApiResponse(responseCode = "200", description = "Order found")
    @ApiResponse(responseCode = "404", description = "Order not found with provided ID")
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @Operation(summary = "Create a new purchase order", description = "Creates a purchase order, updates inventory quantities atomically, and logs system activity.")
    @ApiResponse(responseCode = "201", description = "Order created and stock replenished successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request payload")
    @ApiResponse(responseCode = "404", description = "Supplier or referenced product not found")
    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody OrderRequestDTO request) {
        Order createdOrder = orderService.createOrder(
                request.getSupplierId(),
                request.getProductIds(),
                request.getQuantities(),
                request.getPrices()
        );
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    @Operation(summary = "Get monthly order aggregations", description = "Retrieves order count grouped by month for analytical dashboards.")
    @ApiResponse(responseCode = "200", description = "Monthly statistics returned")
    @GetMapping("/analytics/monthly")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyOrders(@RequestParam(defaultValue = "5") int months) {
        return ResponseEntity.ok(orderService.getOrderCountLastMonths(months));
    }
}
