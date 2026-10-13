package com.ims.ims.controller;
 
import com.ims.ims.model.User;
import com.ims.ims.model.Role;
import com.ims.ims.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
 
/**
 * Controller لإدارة المستخدمين
 * 
 * Endpoints:
 * - GET    /api/users                           → جميع المستخدمين
 * - GET    /api/users/{id}                      → مستخدم برقمه
 * - GET    /api/users/username/{username}       → مستخدم باسمه
 * - GET    /api/users/active                    → المستخدمين النشطين
 * - GET    /api/users/role/{role}               → مستخدمين برتبة
 * - POST   /api/users                           → إنشاء مستخدم
 * - PUT    /api/users/{id}                      → تحديث مستخدم
 * - PATCH  /api/users/{id}/change-password      → تغيير كلمة المرور
 * - PATCH  /api/users/{id}/toggle-status        → تفعيل/تعطيل
 * - DELETE /api/users/{id}                      → حذف مستخدم
 */
@RestController
@RequestMapping("/api/users")
@io.swagger.v3.oas.annotations.tags.Tag(name = "User Management", description = "REST APIs for user administration and roles")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    /**
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    
    /**
     * GET /api/users/username/{username}
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        User user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }
    
    /**
     * GET /api/users/active
     */
    @GetMapping("/active")
    public ResponseEntity<List<User>> getActiveUsers() {
        List<User> users = userService.getActiveUsers();
        return ResponseEntity.ok(users);
    }
    
    /**
     * GET /api/users/role/{role}
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable Role role) {
        List<User> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }
    
    /**
     * POST /api/users
     */
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User created = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    /**
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
        @PathVariable Long id,
        @RequestBody User userDetails
    ) {
        User updated = userService.updateUser(id, userDetails);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * PATCH /api/users/{id}/change-password
     */
    /* --- IGNORE ---
    @PatchMapping("/{id}/change-password")
    public ResponseEntity<User> changePassword(
        @PathVariable Long id,
        @RequestParam String oldPassword,
        @RequestParam String newPassword
    ) {
        User updated = userService.changePassword(id, oldPassword, newPassword);
        return ResponseEntity.ok(updated);
    }
    */
    /**
     * PATCH /api/users/{id}/toggle-status
     */
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<User> toggleUserStatus(@PathVariable Long id) {
        User updated = userService.toggleUserStatus(id);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("تم حذف المستخدم بنجاح");
    }
}
 