package com.ims.ims.controller;

import com.ims.ims.model.Product;
import com.ims.ims.service.ProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST Controller لإدارة المنتجات والمخزون (API Layer)
 * يوفر نقاط اتصال لجميع العمليات الحسابية والإحصائية
 */
@RestController
@RequestMapping("/api/products")
@Tag(name = "Product Management", description = "REST APIs for product catalog, stock levels, and valuation")
public class ProductController {

    @Autowired
    private ProductService productService;

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 1. عمليات القراءة والبحث (Read Endpoints)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/low-stock/{threshold}")
    public ResponseEntity<List<Product>> getLowStock(@PathVariable Integer threshold) {
        return ResponseEntity.ok(productService.getLowStockProducts(threshold));
    }

    @GetMapping("/out-of-stock")
    public ResponseEntity<List<Product>> getOutOfStock() {
        return ResponseEntity.ok(productService.getOutOfStockProducts());
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 2. عمليات الإحصاء والتحليلات (Analytics Endpoints)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * حساب القيمة الإجمالية للمخزون بأسعار التكلفة
     */
    @GetMapping("/stats/inventory-value")
    public ResponseEntity<BigDecimal> getInventoryValue() {
        return ResponseEntity.ok(productService.calculateTotalInventoryValue());
    }

    /**
     * حساب الأرباح المحتملة (المتوقعة) من بيع المخزون الحالي
     */
    @GetMapping("/stats/potential-profit")
    public ResponseEntity<BigDecimal> getPotentialProfit() {
        return ResponseEntity.ok(productService.calculateTotalProfit());
    }

    /**
     * حساب هامش الربح المئوي للمخزون
     */
    @GetMapping("/stats/profit-margin")
    public ResponseEntity<BigDecimal> getProfitMargin() {
        return ResponseEntity.ok(productService.calculateProfitMargin());
    }

    /**
     * حساب متوسط سعر المنتجات المعروضة
     */
    @GetMapping("/stats/average-price")
    public ResponseEntity<BigDecimal> getAveragePrice() {
        return ResponseEntity.ok(productService.calculateAverageProductPrice());
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 3. عمليات التعديل والكتابة (Write Endpoints)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product savedProduct = productService.createProduct(product);
        return new ResponseEntity<>(savedProduct, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        return ResponseEntity.ok(productService.updateProduct(id, product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok("Product deleted successfully");
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 4. عمليات التحكم السريع في المخزون (Stock Patching)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * إضافة كمية للمخزون بشكل مباشر
     */
    @PatchMapping("/{id}/add-stock")
    public ResponseEntity<Product> addStock(
            @PathVariable Long id, 
            @RequestParam Integer quantity) {
        Product updated = productService.addStock(id, quantity);
        return ResponseEntity.ok(updated);
    }

    /**
     * سحب كمية من المخزون بشكل مباشر
     */
    @PatchMapping("/{id}/subtract-stock")
    public ResponseEntity<Product> subtractStock(
            @PathVariable Long id, 
            @RequestParam Integer quantity) {
        Product updated = productService.subtractStock(id, quantity);
        return ResponseEntity.ok(updated);
    }
}