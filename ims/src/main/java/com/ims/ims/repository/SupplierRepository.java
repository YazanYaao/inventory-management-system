package com.ims.ims.repository;

import com.ims.ims.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

/**
 * Repository لإدارة بيانات الموردين
 * * بفضل وراثة JpaRepository، أصبح لديك الآن دوال جاهزة مثل:
 * - findById(Long id) -> ستعمل تلقائياً مع الحقل الجديد id
 * - save(Supplier s)
 * - deleteById(Long id)
 */
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    
    /**
     * البحث عن مورد باسمه الكامل
     */
    Optional<Supplier> findByName(String name);
    
    /**
     * البحث عن موردين يحتوي اسمهم على نص معين (تجاهل حالة الأحرف)
     * مفيد جداً لشريط البحث في واجهة المستخدم
     */
    List<Supplier> findByNameContainingIgnoreCase(String name);
    
    /**
     * التحقق من وجود مورد باسم معين لمنع التكرار
     */
    boolean existsByName(String name);
}