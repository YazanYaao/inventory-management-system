package com.ims.ims.controller;

import com.ims.ims.model.Product;
import com.ims.ims.model.Transaction;
import com.ims.ims.model.User;
import com.ims.ims.service.ProductService;
import com.ims.ims.service.TransactionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller مخصص لواجهة الموظف (Staff Interface)
 * يركز على الاستعلام عن المخزون وتسجيل الحركات اليومية
 */
@Controller
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private ProductService productService;

    @Autowired
    private TransactionService transactionService;

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 1. لوحة التحكم للموظف (Staff Dashboard)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    @GetMapping("/dashboard")
    public String employeeDashboard(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) return "redirect:/login";

        // إحصائيات سريعة للمخزون
        model.addAttribute("totalProducts", productService.getAllProducts().size());
        model.addAttribute("lowStockCount", productService.getLowStockProducts(10).size());

        // جلب آخر 5 عمليات قام بها هذا الموظف حصراً
        List<Transaction> myTransactions = transactionService.getUserTransactions(currentUser.getId());
        List<Transaction> recent = myTransactions.stream().limit(5).toList();

        model.addAttribute("myRecentTransactions", recent);
        model.addAttribute("employeeName", currentUser.getFullName());

        return "employee/dashboard";
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 2. الاستعلام عن المخزون (Inventory Query)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    @GetMapping("/products")
    public String showInventoryQuery(@RequestParam(value = "search", required = false) String search, Model model) {
        List<Product> productList;

        if (search != null && !search.trim().isEmpty()) {
            // البحث بالاسم (مع إرسال null للقسم والمورد حسب توقيع الميثود الجديدة)
            productList = productService.searchProducts(search.trim(), null, null);
        } else {
            productList = productService.getAllProducts();
        }

        model.addAttribute("products", productList);
        model.addAttribute("search", search);
        model.addAttribute("lowStockCount", productService.getLowStockProducts(10).size());

        return "employee/products";
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 3. إدارة تسجيل الحركات (Transaction Entry)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    @GetMapping("/transactions/add")
    public String showAddTransactionForm(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) return "redirect:/login";

        model.addAttribute("transaction", new Transaction());
        model.addAttribute("products", productService.getAllProducts());
        return "employee/add-transaction";
    }

    @PostMapping("/transactions/add")
    public String processTransaction(@ModelAttribute("transaction") Transaction transaction, 
                                     HttpSession session, RedirectAttributes ra) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                ra.addFlashAttribute("error", "انتهت الجلسة، يرجى إعادة تسجيل الدخول.");
                return "redirect:/login";
            }
            
            transaction.setUser(currentUser); // ربط العملية بالموظف الحالي
            transactionService.createTransaction(transaction); // تنفيذ العملية وتحديث المخزون
            
            ra.addFlashAttribute("success", "تم تسجيل الحركة وتحديث المخزون بنجاح!");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", "فشل العملية: " + e.getMessage());
            return "redirect:/employee/transactions/add";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "خطأ تقني: " + e.getMessage());
        }
        return "redirect:/employee/transactions/add";
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 4. سجل العمليات الخاص بي (Personal History)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    @GetMapping("/my-history")
    public String showMyHistory(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) return "redirect:/login";

        List<Transaction> myTransactions = transactionService.getUserTransactions(currentUser.getId());
        model.addAttribute("transactions", myTransactions);
        
        return "employee/my-history";
    }
}