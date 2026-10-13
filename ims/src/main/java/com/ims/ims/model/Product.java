package com.ims.ims.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * نموذج المنتجات - العنصر الأساسي في نظام المخزون
 */
@Entity
@Table(name = "products")
// 1. عند استدعاء delete، نفذ هذا الأمر بدلاً من الحذف الحقيقي
@SQLDelete(sql = "UPDATE products SET deleted = true WHERE product_id=?")
// 2. عند جلب البيانات، اجلب فقط التي لم تُحذف
@Where(clause = "deleted=false")
public class Product {
    
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // الحقول الأساسية (Fields)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id; // المعرف الموحد
    

    // أضف هذا الحقل الجديد
    private boolean deleted = false;

    @NotBlank(message = "رمز المنتج (SKU) مطلوب")
    @Column(unique = true, length = 50)
    private String sku; 
    
    @NotBlank(message = "اسم المنتج مطلوب")
    @Column(nullable = false, length = 255)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @DecimalMin(value = "0.0", message = "Price must be positive")
    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice; 
    
    @Column(name = "sell_price", precision = 10, scale = 2)
    private BigDecimal sellPrice; 
    
    @Min(value = 0, message = "الكمية يجب أن تكون موجبة")
    @Column(name = "stock_quantity")
    private Integer stockQuantity = 0; 

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // العلاقات (Relationships)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;
    
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // التتبع الزمني (Audit Fields)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Constructors
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    
    public Product() {}
    
    public Product(String sku, String name, BigDecimal costPrice, 
                   BigDecimal sellPrice, Integer stockQuantity) {
        this.sku = sku;
        this.name = name;
        this.costPrice = costPrice;
        this.sellPrice = sellPrice;
        this.stockQuantity = stockQuantity;
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Getters and Setters
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() {
        if (price != null) return price;
        if (sellPrice != null) return sellPrice;
        if (costPrice != null) return costPrice;
        return BigDecimal.ZERO;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
        this.costPrice = price;
        this.sellPrice = price;
    }

    public BigDecimal getCostPrice() { return costPrice != null ? costPrice : price; }
    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
        if (this.price == null) this.price = costPrice;
    }

    public BigDecimal getSellPrice() { return sellPrice != null ? sellPrice : price; }
    public void setSellPrice(BigDecimal sellPrice) {
        this.sellPrice = sellPrice;
        if (this.price == null) this.price = sellPrice;
    }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }


    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        syncPrices();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        syncPrices();
    }

    private void syncPrices() {
        if (this.price != null) {
            if (this.costPrice == null) this.costPrice = this.price;
            if (this.sellPrice == null) this.sellPrice = this.price;
        } else if (this.sellPrice != null) {
            this.price = this.sellPrice;
            if (this.costPrice == null) this.costPrice = this.sellPrice;
        } else if (this.costPrice != null) {
            this.price = this.costPrice;
            if (this.sellPrice == null) this.sellPrice = this.costPrice;
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Helper Methods (دوال الحسابات)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    
    public BigDecimal getUnitProfit() {
        BigDecimal sp = getSellPrice();
        BigDecimal cp = getCostPrice();
        if (sp == null || cp == null) return BigDecimal.ZERO;
        return sp.subtract(cp);
    }

    // الدالة التي كانت تنقصك لحساب الربح الإجمالي
    public BigDecimal getTotalProfit() {
        if (stockQuantity == null) return BigDecimal.ZERO;
        return getUnitProfit().multiply(new BigDecimal(stockQuantity));
    }

    public BigDecimal getTotalStockValue() {
        BigDecimal cp = getCostPrice();
        if (cp == null || stockQuantity == null) return BigDecimal.ZERO;
        return cp.multiply(new BigDecimal(stockQuantity));
    }

    public boolean isLowStock(int threshold) {
        return stockQuantity != null && stockQuantity < threshold;
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Override Methods
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", sku='" + sku + '\'' +
                ", name='" + name + '\'' +
                ", stockQuantity=" + stockQuantity +
                ", category=" + (category != null ? category.getName() : "N/A") +
                ", supplier=" + (supplier != null ? supplier.getName() : "N/A") +
                '}';
    }
}