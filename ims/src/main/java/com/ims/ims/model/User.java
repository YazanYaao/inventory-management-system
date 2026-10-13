
// ================================================================================
// 6️⃣ USER MODEL - نموذج المستخدمين
// ================================================================================

package com.ims.ims.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

/**
 * نموذج المستخدمين
 * 
 * كل مستخدم في النظام يجب أن يكون مسجلاً هنا
 */
@Entity
@Table(name = "users")
public class User {
    
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // الحقول (Fields)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;
    
    @NotBlank(message = "اسم المستخدم مطلوب")
    @Column(unique = true, nullable = false, length = 50)
    private String username;  // اسم فريد للدخول
    
    @NotBlank(message = "كلمة المرور مطلوبة")
    @Column(nullable = false)
    private String password;  // سيتم تشفيرها
    
    @NotBlank(message = "الاسم الكامل مطلوب")
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;
    
    @Email(message = "البريد الإلكتروني غير صحيح")
    @Column(unique = true, length = 100)
    private String email;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;  // الدور (Admin, Manager, Staff)
    
    @Column(nullable = false)
    private boolean active = true;  // هل المستخدم نشط
    
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Constructors
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    
    public User() {}
    
    public User(String username, String password, String fullName, 
                String email, Role role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }
    
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Getters and Setters
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Override Methods
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", active=" + active +
                '}';
    }
}