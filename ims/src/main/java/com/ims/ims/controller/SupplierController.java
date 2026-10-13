
// ================================================================================
// 2️⃣ SUPPLIER CONTROLLER
// ================================================================================
 
package com.ims.ims.controller;
 
import com.ims.ims.model.Supplier;
import com.ims.ims.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
 
/**
 * Controller لإدارة الموردين
 * 
 * Endpoints:
 * - GET    /api/suppliers                → الحصول على جميع الموردين
 * - GET    /api/suppliers/{id}           → الحصول على مورد برقمه
 * - GET    /api/suppliers/search?name=   → البحث عن موردين
 * - POST   /api/suppliers                → إنشاء مورد جديد
 * - PUT    /api/suppliers/{id}           → تحديث مورد
 * - DELETE /api/suppliers/{id}           → حذف مورد
 */
@RestController
@RequestMapping("/api/suppliers")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Supplier Management", description = "REST APIs for suppliers and vendor relations")
public class SupplierController {
    
    @Autowired
    private SupplierService supplierService;
    
    /**
     * GET /api/suppliers
     */
    @GetMapping
    public ResponseEntity<List<Supplier>> getAllSuppliers() {
        List<Supplier> suppliers = supplierService.getAllSuppliers();
        return ResponseEntity.ok(suppliers);
    }
    
    /**
     * GET /api/suppliers/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Supplier> getSupplierById(@PathVariable Long id) {
        Supplier supplier = supplierService.getSupplierById(id);
        return ResponseEntity.ok(supplier);
    }
    
    /**
     * GET /api/suppliers/search?name=سام
     * البحث عن موردين
     */
    @GetMapping("/search")
    public ResponseEntity<List<Supplier>> searchSuppliers(
        @RequestParam(name = "name") String namePattern
    ) {
        List<Supplier> suppliers = supplierService.searchSuppliers(namePattern);
        return ResponseEntity.ok(suppliers);
    }
    
    /**
     * POST /api/suppliers
     */
    @PostMapping
    public ResponseEntity<Supplier> createSupplier(@RequestBody Supplier supplier) {
        Supplier created = supplierService.createSupplier(supplier);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    /**
     * PUT /api/suppliers/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Supplier> updateSupplier(
        @PathVariable Long id,
        @RequestBody Supplier supplierDetails
    ) {
        Supplier updated = supplierService.updateSupplier(id, supplierDetails);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * DELETE /api/suppliers/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.ok("تم حذف المورد بنجاح");
    }
}
 