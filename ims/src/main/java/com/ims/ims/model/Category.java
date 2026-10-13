package com.ims.ims.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * نموذج الفئات
 * * الفئات تُستخدم لتنظيم المنتجات
 */
@Entity
@Table(name = "categories")
public class Category {
    
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // الحقول (Fields)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id") // نحتفظ بالاسم القديم في قاعدة البيانات لضمان توافق العلاقات
    private Long id; // التغيير هنا: اصبح id بدلاً من categoryId
    
    @NotBlank(message = "اسم الفئة مطلوب")
    @Column(nullable = false, length = 100)
    private String name;
    
    // العلاقة مع جدول المنتجات
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> products;
    
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Constructors
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    
    public Category() {}
    
    public Category(String name) {
        this.name = name;
    }
    
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Getters and Setters (تم تصحيحها لتناسب id)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public List<Product> getProducts() {
        return products;
    }
    
    public void setProducts(List<Product> products) {
        this.products = products;
    }
    
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Override Methods
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    
    @Override
    public String toString() {
        return "Category{" +
                "id=" + id + // تم التحديث هنا ليعمل الكود
                ", name='" + name + '\'' +
                '}';
    }
}