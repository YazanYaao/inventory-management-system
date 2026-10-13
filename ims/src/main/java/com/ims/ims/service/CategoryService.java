// ================================================================================
// 1️⃣ CATEGORY SERVICE
// ================================================================================
 
package com.ims.ims.service;
 
import com.ims.ims.model.Category;
import com.ims.ims.repository.CategoryRepository;
import com.ims.ims.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
 
/**
 * Service لإدارة الفئات
 * 
 * يوفر عمليات الإنشاء والتعديل والحذف والبحث
 */
@Service
@Transactional
public class CategoryService {
    
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ActivityLogService activityLogService;
    
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // عمليات القراءة (Read)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    
    /**
     * الحصول على جميع الفئات
     * @return قائمة بجميع الفئات
     */
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    
    /**
     * الحصول على فئة برقمها
     * @param id رقم الفئة
     * @return الفئة
     * @throws ResourceNotFoundException إذا لم تجد الفئة
     */
    @Transactional(readOnly = true)
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("الفئة غير موجودة برقم: " + id));
    }
    
    /**
     * البحث عن فئة باسمها
     * @param name اسم الفئة
     * @return الفئة
     * @throws ResourceNotFoundException إذا لم تجد الفئة
     */
    @Transactional(readOnly = true)
    public Category getCategoryByName(String name) {
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("الفئة غير موجودة: " + name));
    }
    
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // عمليات الكتابة (Write)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    
    /**
     * إنشاء فئة جديدة
     * @param category الفئة الجديدة
     * @return الفئة المحفوظة
     * @throws IllegalArgumentException إذا كان الاسم فارغاً أو موجوداً بالفعل
     */
    public Category createCategory(Category category) {
        // التحقق من أن الاسم ليس فارغاً
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("اسم الفئة مطلوب");
        }
        
        // التحقق من عدم وجود فئة بنفس الاسم
        if (categoryRepository.existsByName(category.getName())) {
            throw new IllegalArgumentException("هناك فئة بنفس الاسم بالفعل: " + category.getName());
        }
        
        Category saved = categoryRepository.save(category);
        activityLogService.logActivity("CATEGORY", "New Category Created", "Category #" + saved.getId() + " with the name of " + saved.getName());
        return saved;
    }
    
    /**
     * تحديث فئة موجودة
     * @param id رقم الفئة
     * @param categoryDetails بيانات الفئة الجديدة
     * @return الفئة المحدثة
     */
   public Category updateCategory(Long id, Category categoryDetails) {
        Category category = getCategoryById(id);
        
        if (categoryDetails.getName() != null && !categoryDetails.getName().trim().isEmpty()) {
            Optional<Category> existingCategory = categoryRepository.findByName(categoryDetails.getName());
            
            // التعديل هنا: استخدم getId() بدلاً من getCategoryId()
            if (existingCategory.isPresent() && !existingCategory.get().getId().equals(id)) {
                throw new IllegalArgumentException("هناك فئة بنفس الاسم بالفعل");
            }
            category.setName(categoryDetails.getName());
        }
        
        return categoryRepository.save(category);
    }
    
    /**
     * حذف فئة
     * @param id رقم الفئة
     */
    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);
        
        // التحقق من عدم وجود منتجات في هذه الفئة
        if (!category.getProducts().isEmpty()) {
            throw new IllegalArgumentException("لا يمكن حذف فئة تحتوي على منتجات. الرجاء حذف المنتجات أولاً.");
        }
        
        categoryRepository.delete(category);
    }
    
    /**
     * التحقق من وجود فئة
     * @param id رقم الفئة
     * @return true إذا كانت الفئة موجودة
     */
    @Transactional(readOnly = true)
    public boolean categoryExists(Long id) {
        return categoryRepository.existsById(id);
    }

    @Transactional(readOnly = true)
public List<Category> searchCategories(String name) {
    if (name != null && !name.trim().isEmpty()) {
        // إذا كان هناك نص بحث، ابحث في قاعدة البيانات
        return categoryRepository.findByNameContainingIgnoreCase(name.trim());
    }
    // إذا كان البحث فارغاً، ارجع كل الفئات
    return categoryRepository.findAll();
}


}
