package com.ims.ims.service;

import com.ims.ims.exception.ResourceNotFoundException;
import com.ims.ims.model.Order;
import com.ims.ims.model.Product;
import com.ims.ims.model.Supplier;
import com.ims.ims.repository.OrderRepository;
import com.ims.ims.repository.ProductRepository;
import com.ims.ims.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ActivityLogService activityLogService;

    @InjectMocks
    private OrderService orderService;

    private Supplier testSupplier;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testSupplier = new Supplier();
        testSupplier.setId(10L);
        testSupplier.setName("Global Supplies");

        testProduct = new Product();
        testProduct.setId(100L);
        testProduct.setName("Industrial Monitor");
        testProduct.setStockQuantity(10);
    }

    @Test
    @DisplayName("Should successfully create order, increment stock, and log activity")
    void testCreateOrder_Success() {
        when(supplierRepository.findById(10L)).thenReturn(Optional.of(testSupplier));
        when(productRepository.findById(100L)).thenReturn(Optional.of(testProduct));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(1L);
            return o;
        });

        Order createdOrder = orderService.createOrder(
                10L,
                List.of(100L),
                List.of(5),
                List.of(new BigDecimal("150.00"))
        );

        assertNotNull(createdOrder);
        assertEquals(1, createdOrder.getItems().size());
        assertEquals(new BigDecimal("750.00"), createdOrder.getTotalAmount());
        assertEquals(15, testProduct.getStockQuantity()); // 10 + 5 replenished stock

        verify(productRepository, times(1)).save(testProduct);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(activityLogService, times(1)).logActivity(eq("ORDER"), anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when supplier does not exist")
    void testCreateOrder_SupplierNotFound() {
        when(supplierRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                orderService.createOrder(999L, List.of(100L), List.of(2), List.of(new BigDecimal("50.00")))
        );

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should aggregate monthly orders correctly for dashboards")
    void testGetMonthlyOrdersAggregation() {
        when(orderRepository.countOrdersGroupedByMonth(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        List<Map<String, Object>> aggregation = orderService.getOrderCountLastMonths(5);

        assertNotNull(aggregation);
        assertEquals(5, aggregation.size());
        assertTrue(aggregation.get(0).containsKey("name"));
        assertTrue(aggregation.get(0).containsKey("value"));
    }
}
