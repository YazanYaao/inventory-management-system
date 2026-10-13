package com.ims.ims.repository;
 
import com.ims.ims.model.User;
import com.ims.ims.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;
 
/**
 * Repository لإدارة بيانات المستخدمين
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * البحث عن مستخدم باسم المستخدم
     * SELECT * FROM users WHERE username = ?
     * 
     * @param username - اسم الدخول
     * @return Optional يحتوي على المستخدم
     */
    Optional<User> findByUsername(String username);
    
    /**
     * البحث عن مستخدم بالبريد الإلكتروني
     * SELECT * FROM users WHERE email = ?
     * 
     * @param email - البريد الإلكتروني
     * @return Optional يحتوي على المستخدم
     */
    Optional<User> findByEmail(String email);
    
    /**
     * التحقق من وجود مستخدم باسم معين
     * SELECT COUNT(*) FROM users WHERE username = ?
     */
    boolean existsByUsername(String username);
    
    /**
     * التحقق من وجود مستخدم ببريد معين
     * SELECT COUNT(*) FROM users WHERE email = ?
     */
    boolean existsByEmail(String email);
    
    /**
     * الحصول على جميع المستخدمين النشطين
     * SELECT * FROM users WHERE active = true
     * 
     * @return قائمة بجميع المستخدمين النشطين
     */
    List<User> findByActiveTrue();
    
    /**
     * الحصول على جميع المستخدمين غير النشطين
     * SELECT * FROM users WHERE active = false
     */
    List<User> findByActiveFalse();
    
    /**
     * الحصول على جميع المستخدمين برتبة معينة
     * SELECT * FROM users WHERE role = ?
     * 
     * @param role - الدور (ADMIN, MANAGER, STAFF)
     * @return قائمة بالمستخدمين برتبة معينة
     */
    List<User> findByRole(Role role);
    
    /**
     * البحث عن مستخدمين بناءً على اسمهم الكامل (غير حساس لحالة الأحرف)
     * SELECT * FROM users WHERE LOWER(full_name) LIKE LOWER(?)
     * 
     * @param fullName - جزء من الاسم الكامل
     * @return قائمة بالمستخدمين المطابقين
     */
    List<User> findByFullNameContainingIgnoreCase(String fullName);

    // أضف هذا السطر في UserRepository.java
    Optional<User> findByUsernameAndPassword(String username, String password);
}