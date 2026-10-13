package com.ims.ims.controller;

import com.ims.ims.exception.ResourceNotFoundException;
import com.ims.ims.model.Category;
import com.ims.ims.model.Product;
import com.ims.ims.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.ims.ims.exception.GlobalExceptionHandler;

@WebMvcTest(controllers = {ProductController.class, GlobalExceptionHandler.class})
@DisplayName("ProductController REST API Tests")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    @DisplayName("GET /api/products should return 200 OK with product list")
    void testGetAllProducts() throws Exception {
        Category cat = new Category();
        cat.setName("Hardware");

        Product p = new Product();
        p.setId(1L);
        p.setName("SSD 1TB");
        p.setSku("HW-SSD-1TB");
        p.setPrice(new BigDecimal("99.99"));
        p.setStockQuantity(30);
        p.setCategory(cat);

        when(productService.getAllProducts()).thenReturn(List.of(p));

        mockMvc.perform(get("/api/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("SSD 1TB"))
                .andExpect(jsonPath("$[0].sku").value("HW-SSD-1TB"))
                .andExpect(jsonPath("$[0].stockQuantity").value(30));
    }

    @Test
    @DisplayName("GET /api/products/{id} should return 200 OK when product exists")
    void testGetProductById_Found() throws Exception {
        Product p = new Product();
        p.setId(5L);
        p.setName("Mechanical Keyboard");
        p.setSku("HW-KEY-001");
        p.setPrice(new BigDecimal("75.00"));

        when(productService.getProductById(5L)).thenReturn(p);

        mockMvc.perform(get("/api/products/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Mechanical Keyboard"));
    }

    @Test
    @DisplayName("GET /api/products/{id} should return 404 when product is not found")
    void testGetProductById_NotFound() throws Exception {
        when(productService.getProductById(999L))
                .thenThrow(new ResourceNotFoundException("Product not found with ID: 999"));

        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Product not found with ID: 999"));
    }

    @Test
    @DisplayName("GET /api/products/stats/inventory-value should return 200 with total valuation")
    void testGetInventoryValue() throws Exception {
        when(productService.calculateTotalInventoryValue()).thenReturn(new BigDecimal("12500.50"));

        mockMvc.perform(get("/api/products/stats/inventory-value"))
                .andExpect(status().isOk())
                .andExpect(content().string("12500.50"));
    }
}
