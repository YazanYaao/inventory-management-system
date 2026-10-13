package com.ims.ims.service;

import com.ims.ims.model.Supplier;
import com.ims.ims.repository.SupplierRepository;
import com.ims.ims.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SupplierService {
    
    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Supplier getSupplierById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("المورد غير موجود برقم: " + id));
    }

    @Transactional(readOnly = true)
    public Supplier getSupplierByName(String name) {
        return supplierRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("المورد غير موجود بالاسم: " + name));
    }

    @Transactional(readOnly = true)
    public List<Supplier> searchSuppliers(String namePattern) {
        return supplierRepository.findByNameContainingIgnoreCase(namePattern);
    }

    public Supplier createSupplier(Supplier supplier) {
        if (supplier.getName() == null || supplier.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("اسم المورد مطلوب");
        }
        if (supplierRepository.existsByName(supplier.getName())) {
            throw new IllegalArgumentException("هناك مورد بنفس الاسم بالفعل");
        }
        Supplier saved = supplierRepository.save(supplier);
        activityLogService.logActivity("SUPPLIER", "New Supplier Created", "Supplier #" + saved.getId() + " with the name of " + saved.getName());
        return saved;
    }

    /**
     * تحديث بيانات المورد
     */
    public Supplier updateSupplier(Long id, Supplier supplierDetails) {
        Supplier supplier = getSupplierById(id);
        
        // تحديث الاسم (موجود عندك وصحيح)
        if (supplierDetails.getName() != null && !supplierDetails.getName().trim().isEmpty()) {
            Optional<Supplier> existing = supplierRepository.findByName(supplierDetails.getName());
            if (existing.isPresent() && !existing.get().getId().equals(id)) {
                throw new IllegalArgumentException("هناك مورد آخر يحمل هذا الاسم بالفعل");
            }
            supplier.setName(supplierDetails.getName());
        }
        
        // تحديث البيانات الجديدة
        if (supplierDetails.getContactInfo() != null) {
            supplier.setContactInfo(supplierDetails.getContactInfo());
        }
        
        // --- أضف هذه الأسطر هنا ---
        if (supplierDetails.getPhone() != null) {
            supplier.setPhone(supplierDetails.getPhone());
        }
        
        if (supplierDetails.getEmail() != null) {
            supplier.setEmail(supplierDetails.getEmail());
        }
        
        if (supplierDetails.getAddress() != null) {
            supplier.setAddress(supplierDetails.getAddress());
        }
        // -------------------------
        
        return supplierRepository.save(supplier);
    }

    public void deleteSupplier(Long id) {
        Supplier supplier = getSupplierById(id);
        
        // منع حذف المورد إذا كان مرتبطاً بمنتجات (Integrity Constraint)
        if (supplier.getProducts() != null && !supplier.getProducts().isEmpty()) {
            throw new IllegalArgumentException("لا يمكن حذف مورد مرتبط بمنتجات في المخزن. قم بتغيير مورد المنتجات أولاً.");
        }
        
        supplierRepository.delete(supplier);
    }
}