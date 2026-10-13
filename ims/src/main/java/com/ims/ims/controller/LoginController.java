package com.ims.ims.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ims.ims.model.LoginDTO;
import com.ims.ims.model.Role;
import com.ims.ims.model.User;
import com.ims.ims.repository.UserRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

@Controller
public class LoginController {
    
    @Autowired 
    private UserRepository userRepository;

    
    @GetMapping("/")
    public String Home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String LoginPage(Model model) {
        model.addAttribute("loginDTO", new LoginDTO());
        return "login";
    }

    @PostMapping("/login")
public String login(@Valid @ModelAttribute LoginDTO loginDTO, 
                   BindingResult result, 
                   HttpSession session, 
                   RedirectAttributes redirectAttributes) {
    
    // 1. التحقق من أخطاء الإدخال (Validation)
    if (result.hasErrors()) {
        return "login";
    }
    
    // 2. البحث عن المستخدم في قاعدة البيانات
    Optional<User> userOpt = userRepository.findByUsernameAndPassword(
        loginDTO.getUsername(), loginDTO.getPassword());

    if (userOpt.isPresent() && userOpt.get().isActive()) {
        User user = userOpt.get();
        
        // 3. تخزين معلومات المستخدم في الجلسة (Session)
        session.setAttribute("user", user);
        session.setAttribute("username", user.getUsername());
        session.setAttribute("role", user.getRole().toString());

        // 4. التوجيه حسب الصلاحيات (Role-Based Redirection)
        if (user.getRole() == Role.ADMIN) {
            return "redirect:/admin/dashboard";
        } 
        else if (user.getRole() == Role.STAFF) {
            return "redirect:/employee/dashboard";
        } 
        else {
            return "redirect:/user/dashboard";
        }
        
    } else {
        // 5. في حال الخطأ في البيانات أو الحساب غير نشط
        redirectAttributes.addFlashAttribute("error", "اسم المستخدم أو كلمة المرور غير صحيحة، أو الحساب معطل.");
        return "redirect:/login";
    }
}

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Logout - تسجيل الخروج
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate(); // مسح الجلسة بالكامل
        redirectAttributes.addFlashAttribute("success", "تم تسجيل الخروج بنجاح.");
        return "redirect:/login";
    }
}
