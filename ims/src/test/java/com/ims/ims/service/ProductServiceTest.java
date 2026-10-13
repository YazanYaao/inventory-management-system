package com.ims.ims.service;

import com.ims.ims.exception.ResourceNotFoundException;
import com.ims.ims.model.Category;
import com.ims.ims.model.Product;
import com.ims.ims.model.Supplier;
import com.ims.ims.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ActivityLogService activityLogService;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Electronics");

        Supplier supplier = new Supplier();
        supplier.setId(1L);
        supplier.setName("Tech Corp");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Wireless Mouse");
        testProduct.setSku("ELEC-1001");
        testProduct.setCostPrice(new BigDecimal("15.00"));
        testProduct.setSellPrice(new BigDecimal("25.00"));
        testProduct.setPrice(new BigDecimal("25.00"));
        testProduct.setStockQuantity(50);
        testProduct.setCategory(category);
        testProduct.setSupplier(supplier);
    }

    @Test
    @DisplayName("Should return product when found by ID")
    void testGetProductById_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        Product result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals("Wireless Mouse", result.getName());
        assertEquals("ELEC-1001", result.getSku());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when product ID does not exist")
    void testGetProductById_NotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(99L));
        verify(productRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Should add stock quantity successfully")
    void testAddStock_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product updated = productService.addStock(1L, 20);

        assertEquals(70, updated.getStockQuantity());
        verify(productRepository, times(1)).save(testProduct);
    }

    @Test
    @DisplayName("Should subtract stock quantity when available")
    void testSubtractStock_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product updated = productService.subtractStock(1L, 30);

        assertEquals(20, updated.getStockQuantity());
        verify(productRepository, times(1)).save(testProduct);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when subtraction exceeds available stock")
    void testSubtractStock_InsufficientStock() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        assertThrows(IllegalArgumentException.class, () -> productService.subtractStock(1L, 100));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should accurately calculate total inventory valuation based on cost price")
    void testCalculateTotalInventoryValue() {
        Product p2 = new Product();
        p2.setId(2L);
        p2.setCostPrice(new BigDecimal("10.00"));
        p2.setStockQuantity(20);

        when(productRepository.findAll()).thenReturn(Arrays.asList(testProduct, p2));

        // testProduct: 50 * 25.00 = 1250.00 (price synced)
        // p2: 20 * 10.00 = 200.00
        // Total = 1450.00
        BigDecimal totalValue = productService.calculateTotalInventoryValue();

        assertEquals(new BigDecimal("1450.00"), totalValue);
    }

    @Test
    @DisplayName("Should retrieve low stock products based on threshold")
    void testGetLowStockProducts() {
        testProduct.setStockQuantity(3);
        Product p2 = new Product();
        p2.setId(2L);
        p2.setStockQuantity(50);

        when(productRepository.findAll()).thenReturn(List.of(testProduct, p2));

        List<Product> lowStock = productService.getLowStockProducts(5);

        assertEquals(1, lowStock.size());
        assertEquals("Wireless Mouse", lowStock.get(0).getName());
    }
}
