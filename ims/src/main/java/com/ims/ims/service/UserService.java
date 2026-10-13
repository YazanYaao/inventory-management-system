

// ================================================================================
// 4️⃣ USER SERVICE
// ================================================================================
 

package com.ims.ims.service;
 
import com.ims.ims.model.User;
import com.ims.ims.model.Role;
import com.ims.ims.repository.UserRepository;
import com.ims.ims.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

//import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
 
/**
 * Service لإدارة المستخدمين
 * 
 * يتعامل مع:
 * - إنشاء وتحديث حذف المستخدمين
 * - تشفير كلمات المرور
 * - إدارة الأدوار والصلاحيات
 */

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    /*
    @Autowired
    private PasswordEncoder passwordEncoder;
    */
    /**
     * الحصول على جميع المستخدمين
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * الحصول على مستخدم برقمه
     */
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("المستخدم غير موجود برقم: " + id));
    }
    
    /**
     * البحث عن مستخدم باسم المستخدم
     */
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("المستخدم غير موجود: " + username));
    }
    
    /**
     * الحصول على جميع المستخدمين النشطين
     */
    @Transactional(readOnly = true)
    public List<User> getActiveUsers() {
        return userRepository.findByActiveTrue();
    }
    
    /**
     * الحصول على جميع المستخدمين غير النشطين
     */
    @Transactional(readOnly = true)
    public List<User> getInactiveUsers() {
        return userRepository.findByActiveFalse();
    }
    
    /**
     * الحصول على جميع المستخدمين برتبة معينة
     */
    @Transactional(readOnly = true)
    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }
    
    /**
     * إنشاء مستخدم جديد
     */
    public User createUser(User user) {
        // التحقق من البيانات المطلوبة
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("اسم المستخدم مطلوب");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("كلمة المرور مطلوبة");
        }
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("الاسم الكامل مطلوب");
        }
        
        // التحقق من عدم وجود مستخدم بنفس الاسم
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("هناك مستخدم بنفس الاسم بالفعل");
        }
        
        // التحقق من البريد الإلكتروني
        if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            if (userRepository.existsByEmail(user.getEmail())) {
                throw new IllegalArgumentException("البريد الإلكتروني مستخدم بالفعل");
            }
        }
        /* 
        // تشفير كلمة المرور
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        */
        // تعيين القيم الافتراضية
        if (user.getRole() == null) {
            user.setRole(Role.STAFF);
        }
        user.setActive(true);
        
        return userRepository.save(user);
    }
    
    /**
     * تحديث مستخدم
     */
    public User updateUser(Long id, User userDetails) {
        User user = getUserById(id);
        
        // تحديث البيانات الشخصية
        if (userDetails.getFullName() != null && !userDetails.getFullName().trim().isEmpty()) {
            user.setFullName(userDetails.getFullName());
        }
        
        if (userDetails.getEmail() != null && !userDetails.getEmail().trim().isEmpty()) {
            Optional<User> existing = userRepository.findByEmail(userDetails.getEmail());
            if (existing.isPresent() && !existing.get().getId().equals(id)) {
                throw new IllegalArgumentException("البريد الإلكتروني مستخدم بالفعل");
            }
            user.setEmail(userDetails.getEmail());
        }
        
        // تحديث الدور
        if (userDetails.getRole() != null) {
            user.setRole(userDetails.getRole());
        }
        
        return userRepository.save(user);
    }
    
    /**
     * تغيير كلمة المرور
     */
    /* 
    public User changePassword(Long id, String oldPassword, String newPassword) {
        User user = getUserById(id);
        
        // التحقق من كلمة المرور القديمة
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("كلمة المرور القديمة غير صحيحة");
        }
        
        // التحقق من أن كلمة المرور الجديدة ليست فارغة
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("كلمة المرور الجديدة مطلوبة");
        }
        
        // تشفير وحفظ كلمة المرور الجديدة
        user.setPassword(passwordEncoder.encode(newPassword));
        
        return userRepository.save(user);
    }*/
    
    /**
     * تفعيل/تعطيل مستخدم
     */
    public User toggleUserStatus(Long id) {
        User user = getUserById(id);
        user.setActive(!user.isActive());
        return userRepository.save(user);
    }
    
    /**
     * حذف مستخدم
     */
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }
}
 
