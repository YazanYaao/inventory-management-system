
/*
 * Controllers هي طبقة التواصل مع العميل (Presentation Layer)
 * 
 * وظائفها:
 * - استقبال الطلبات (HTTP Requests) من العميل
 * - استدعاء Services لمعالجة البيانات
 * - إرجاع الردود (HTTP Responses) للعميل
 * - معالجة الأخطاء
 * 
 * الفرق بين Repositories و Services و Controllers:
 * 
 * Repository (طبقة البيانات):
 * ├─ التواصل مع قاعدة البيانات
 * └─ عمليات CRUD أساسية
 * 
 * Service (طبقة المنطق):
 * ├─ منطق الأعمال المعقد
 * ├─ التحقق من البيانات
 * └─ إدارة العلاقات
 * 
 * Controller (طبقة التواصل):
 * ├─ استقبال الطلبات
 * ├─ استدعاء Services
 * └─ إرجاع الردود
 * 
 */
 
// ================================================================================
// 1️⃣ CATEGORY CONTROLLER
// ================================================================================
 
package com.ims.ims.controller;
 
import com.ims.ims.model.Category;
import com.ims.ims.service.CategoryService;
import com.ims.ims.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
 
/**
 * Controller لإدارة الفئات
 * 
 * Endpoints:
 * - GET    /api/categories              → الحصول على جميع الفئات
 * - GET    /api/categories/{id}         → الحصول على فئة برقمها
 * - POST   /api/categories              → إنشاء فئة جديدة
 * - PUT    /api/categories/{id}         → تحديث فئة
 * - DELETE /api/categories/{id}         → حذف فئة
 */
@RestController
@RequestMapping("/api/categories")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Category Management", description = "REST APIs for product categories")
public class CategoryController {
    
    @Autowired
    private CategoryService categoryService;
    
    /**
     * GET /api/categories
     * الحصول على جميع الفئات
     */
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }
    
    /**
     * GET /api/categories/{id}
     * الحصول على فئة برقمها
     * @param id رقم الفئة
     */
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }
    
    /**
     * POST /api/categories
     * إنشاء فئة جديدة
     * @param category بيانات الفئة الجديدة
     */
    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        Category created = categoryService.createCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    /**
     * PUT /api/categories/{id}
     * تحديث فئة
     * @param id رقم الفئة
     * @param categoryDetails البيانات الجديدة
     */
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(
        @PathVariable Long id,
        @RequestBody Category categoryDetails
    ) {
        Category updated = categoryService.updateCategory(id, categoryDetails);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * DELETE /api/categories/{id}
     * حذف فئة
     * @param id رقم الفئة
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok("تم حذف الفئة بنجاح");
    }
}