package com.ims.ims.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ims.ims.model.Category;
import java.util.List;
import java.util.Optional;



/**
 * Repository لإدارة بيانات الفئات
 * 
 * JpaRepository<Entity, PrimaryKeyType>
 *   - Entity: نوع البيانات (Category)
 *   - PrimaryKeyType: نوع المفتاح الأساسي (Long)
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
   

       List<Category> findByNameContainingIgnoreCase(String name);


    
    /**
     * البحث عن فئة برقمها
     * SELECT * FROM categories WHERE category_id = ?
     */
    // Optional من JpaRepository مباشرة
    // findById(Long id) - موروث
    
    /**
     * البحث عن فئة باسمها
     * SELECT * FROM categories WHERE name = ?
     * 
     * @param name - اسم الفئة
     * @return Optional يحتوي على الفئة أو فارغ
     */

    Optional<Category>findByName(String name);

       /**
     * التحقق من وجود فئة باسم معين
     * SELECT COUNT(*) FROM categories WHERE name = ?
     * 
     * @param name - اسم الفئة
     * @return true إذا كانت الفئة موجودة
     */
    boolean existsByName(String name);

    

}
