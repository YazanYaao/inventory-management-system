package com.ims.ims.controller;

import com.ims.ims.model.*;
import com.ims.ims.repository.*;
import com.ims.ims.service.ProductService;
import com.ims.ims.service.TransactionService;
import com.ims.ims.service.UserService;
import com.ims.ims.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import com.ims.ims.service.CategoryService;
import com.ims.ims.service.OrderService;
import com.ims.ims.service.ActivityLogService;


@Controller
@RequestMapping("/admin")
public class AdminController {

@Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserRepository userRepository;

    @Autowired private CategoryRepository categoryRepository;

    @Autowired private SupplierRepository supplierRepository;

    @Autowired private SupplierService supplierService;
    
    @Autowired
    private TransactionService transactionService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ActivityLogService activityLogService;



    private boolean isAdmin(HttpSession session){
        User user = (User) session.getAttribute("user");
        return user != null && user.getRole() == Role.ADMIN;
    }

    
    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        try {
            // === 1. البطاقات الأربع التشغيلية (4 Operational Cards) ===
            // 1. Total Products
            long totalProducts = productService.getAllProducts().size();
            model.addAttribute("totalProducts", totalProducts);

            // 2. Low Stock (Threshold <= 5 matching reference project)
            List<Product> lowStockProducts = productService.getLowStockProducts(5);
            long lowStockCount = lowStockProducts.size();
            model.addAttribute("lowStockCount", lowStockCount);
            model.addAttribute("lowStockStatus", lowStockCount == 0 ? "Stock in order" : "Requires attention");

            // 3. Monthly Orders
            long monthlyOrdersCount = orderService.countOrdersThisMonth();
            model.addAttribute("monthlyOrdersCount", monthlyOrdersCount);

            // 4. Active Suppliers
            long activeSuppliersCount = supplierService.getAllSuppliers().size();
            model.addAttribute("activeSuppliersCount", activeSuppliersCount);

            // === 2. الرسوم البيانية التحليلية (2 Charts) ===
            // Chart 1: Orders by Month (Monthly order analysis) - Last 5 months
            List<Map<String, Object>> monthlyOrders = orderService.getOrderCountLastMonths(5);
            List<String> orderMonthLabels = new ArrayList<>();
            List<Long> orderMonthValues = new ArrayList<>();
            for (Map<String, Object> m : monthlyOrders) {
                orderMonthLabels.add((String) m.get("name"));
                orderMonthValues.add((Long) m.get("value"));
            }
            model.addAttribute("orderMonthLabels", orderMonthLabels);
            model.addAttribute("orderMonthValues", orderMonthValues);

            // Chart 2: Distribution by Category (Products by category) - stock per category
            List<Map<String, Object>> categoryDist = productService.getCategoryStockDistribution();
            List<String> categoryDistLabels = new ArrayList<>();
            List<Integer> categoryDistValues = new ArrayList<>();
            for (Map<String, Object> cd : categoryDist) {
                categoryDistLabels.add((String) cd.get("category"));
                categoryDistValues.add((Integer) cd.get("quantity"));
            }
            model.addAttribute("categoryDistLabels", categoryDistLabels);
            model.addAttribute("categoryDistValues", categoryDistValues);

            // === 3. سجل النشاطات الأخيرة (Recent Activity) ===
            List<ActivityLog> recentActivities = activityLogService.getRecentActivities();
            model.addAttribute("recentActivities", recentActivities);

            return "admin/dashboard";

        } catch (Exception e) {
            System.err.println("Error loading dashboard: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error loading dashboard metrics");
            return "admin/dashboard";
        }
    }

 
    
        // ========== User Management ==========

    @GetMapping("/users")
    public String listUsers(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "admin/users";


    }

    @GetMapping("/users/add")
    public String showAddUser(HttpSession session, Model model){
        if(!isAdmin(session)){
            return "redirect:/login";
        }
        model.addAttribute("user", new User());
        model.addAttribute("roles", Role.values());
        return "admin/add-user";
    }

    @PostMapping("/users/add")
public String addUser(@Valid @ModelAttribute User user, 
                         BindingResult result, 
                         HttpSession session, 
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        // Check if username already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            result.rejectValue("username", "error.user", "اسم المستخدم موجود مسبقاً");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            result.rejectValue("email", "error.user", "البريد الإلكتروني موجود مسبقاً");
        }

        // If there are validation errors, return to form
        if (result.hasErrors()) {
            model.addAttribute("roles", Role.values());
            return "admin/add-user";
        }

        // Save the user
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "تم إضافة المستخدم بنجاح");
        return "redirect:/admin/users";
    }



    @GetMapping("users/edit/{id}")
    public String showEditUser(@PathVariable Long id, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        Optional<User> optionalUser = userRepository.findById(id);
        if (!optionalUser.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "المستخدم غير موجود");
            return "redirect:/admin/users";
        }

        model.addAttribute("user", optionalUser.get());
        model.addAttribute("roles", Role.values());
         return "admin/edit-user";
    }

     // Process edit user
    @PostMapping("/users/edit/{id}")
    public String editUser(@PathVariable Long id,
                          @Valid @ModelAttribute User user, 
                          BindingResult result, 
                          HttpSession session,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        // Get existing user
        Optional<User> existingUserOpt = userRepository.findById(id);
        if (!existingUserOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "المستخدم غير موجود");
            return "redirect:/admin/users";
        }

        User existingUser = existingUserOpt.get();

        // Check if username is changed and already exists
        if (!existingUser.getUsername().equals(user.getUsername()) && 
            userRepository.existsByUsername(user.getUsername())) {
            result.rejectValue("username", "error.user", "اسم المستخدم موجود مسبقاً");
        }

        // Check if email is changed and already exists
        if (user.getEmail() != null && !user.getEmail().equals(existingUser.getEmail()) && 
            userRepository.existsByEmail(user.getEmail())) {
            result.rejectValue("email", "error.user", "البريد الإلكتروني موجود مسبقاً");
        }

        // If there are validation errors, return to form
        if (result.hasErrors()) {
            model.addAttribute("roles", Role.values());
             return "admin/edit-user";
        }

        // Update user data
        user.setId(id); // Make sure we keep the same ID
        userRepository.save(user);
        
        redirectAttributes.addFlashAttribute("success", "تم تعديل المستخدم بنجاح");
        return "redirect:/admin/users";
    }

      @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        Optional<User> optionalUser = userRepository.findById(id);
        if (!optionalUser.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "المستخدم غير موجود");
            return "redirect:/admin/users";
        }

        User current = (User) session.getAttribute("user");
        if (current != null && id != null && current.getId() == id.longValue()) {
            redirectAttributes.addFlashAttribute("error", "لا يمكنك حذف حسابك الحالي");
            return "redirect:/admin/users";
        }

        try {
            userRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "تم حذف المستخدم بنجاح");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "حدث خطأ أثناء الحذف. تأكد من عدم وجود بيانات مرتبطة بالمستخدم.");
        }

        return "redirect:/admin/users";
    }

        @GetMapping("/products")
            public String listProducts(HttpSession session, Model model, 
                                    @RequestParam(required = false) String search) {
                if (!isAdmin(session)) return "redirect:/login";

                List<Product> products;
                if (search != null && !search.trim().isEmpty()) {
                    products = productRepository.findByNameContainingIgnoreCase(search.trim());
                } else {
                    products = productRepository.findAll();
                }

                model.addAttribute("products", products);
                model.addAttribute("search", search);
                return "admin/products";
            }

            @GetMapping("/products/add")
            public String showAddProductForm(HttpSession session, Model model) {
                if (!isAdmin(session)) return "redirect:/login";
                
                model.addAttribute("product", new Product());
                model.addAttribute("categories", categoryRepository.findAll());
                model.addAttribute("suppliers", supplierRepository.findAll());
                return "admin/add-product";
            }

            @PostMapping("/products/add")
            public String addProduct(@Valid @ModelAttribute Product product, BindingResult result, 
                                    HttpSession session, RedirectAttributes ra, Model model) {
                if (!isAdmin(session)) return "redirect:/login";

                if (result.hasErrors()) {
                    model.addAttribute("categories", categoryRepository.findAll());
                    model.addAttribute("suppliers", supplierRepository.findAll());
                    return "admin/add-product";
                }

                productRepository.save(product);
                ra.addFlashAttribute("success", "تم إضافة المنتج '" + product.getName() + "' بنجاح!");
                return "redirect:/admin/products";
            }

            // 1. عرض صفحة التعديل (GET)
            @GetMapping("/products/edit/{id}")
            public String showEditProductForm(@PathVariable Long id, HttpSession session, Model model, RedirectAttributes ra) {
                if (!isAdmin(session)) return "redirect:/login";

                Optional<Product> productOpt = productRepository.findById(id);
                if (productOpt.isPresent()) {
                    model.addAttribute("product", productOpt.get());
                    model.addAttribute("categories", categoryRepository.findAll()); // ضروري للقائمة المنسدلة
                    model.addAttribute("suppliers", supplierRepository.findAll());   // ضروري للقائمة المنسدلة
                    return "admin/edit-product";
                } else {
                    ra.addFlashAttribute("error", "المنتج غير موجود!");
                    return "redirect:/admin/products";
                }
            }

            // 2. معالجة بيانات التعديل (POST)
            @PostMapping("/products/edit/{id}")
            public String updateProduct(@PathVariable Long id, 
                                        @Valid @ModelAttribute("product") Product product, 
                                        BindingResult result, 
                                        HttpSession session, 
                                        RedirectAttributes ra, 
                                        Model model) {
                if (!isAdmin(session)) return "redirect:/login";

                if (result.hasErrors()) {
                    // في حال وجود أخطاء، يجب إعادة تحميل القوائم قبل العودة للصفحة
                    model.addAttribute("categories", categoryRepository.findAll());
                    model.addAttribute("suppliers", supplierRepository.findAll());
                    return "admin/edit-product";
                }

                // التأكد من تعيين الـ ID القادم من الرابط للكائن لضمان التحديث
                product.setId(id);
                
                try {
                    productRepository.save(product);
                    ra.addFlashAttribute("success", "تم تحديث بيانات المنتج '" + product.getName() + "' بنجاح!");
                } catch (Exception e) {
                    ra.addFlashAttribute("error", "حدث خطأ أثناء التحديث: " + e.getMessage());
                }

                return "redirect:/admin/products";
            }

            @PostMapping("/products/delete/{id}")
        public String deleteProduct(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
            if (!isAdmin(session)) return "redirect:/login";
            
            // بفضل @SQLDelete في الموديل، هذا السطر سيحول deleted إلى true ولن يمسح البيانات
            productRepository.deleteById(id);
            
            ra.addFlashAttribute("success", "تم نقل المنتج إلى الأرشيف (حذف ناعم) بنجاح!");
            return "redirect:/admin/products";
        }

    // ==========================================
    // إدارة طلبات التوريد (Supplier Orders)
    // ==========================================

    @GetMapping("/orders")
    public String listOrders(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";
        model.addAttribute("orders", orderService.getAllOrders());
        return "admin/orders";
    }

    @GetMapping("/orders/add")
    public String showAddOrderForm(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";
        model.addAttribute("suppliers", supplierService.getAllSuppliers());
        model.addAttribute("products", productService.getAllProducts());
        return "admin/add-order";
    }

    @PostMapping("/orders/add")
    public String addOrder(@RequestParam Long supplierId,
                           @RequestParam(name = "productIds", required = false) List<Long> productIds,
                           @RequestParam(name = "quantities", required = false) List<Integer> quantities,
                           @RequestParam(name = "prices", required = false) List<BigDecimal> prices,
                           HttpSession session,
                           RedirectAttributes ra) {
        if (!isAdmin(session)) return "redirect:/login";

        if (productIds == null || productIds.isEmpty() || quantities == null || quantities.isEmpty()) {
            ra.addFlashAttribute("error", "Please select at least one product with quantity.");
            return "redirect:/admin/orders/add";
        }

        try {
            Order order = orderService.createOrder(supplierId, productIds, quantities, prices);
            ra.addFlashAttribute("success", "Order #" + order.getOrderNumber() + " created successfully!");
            return "redirect:/admin/orders";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error creating order: " + e.getMessage());
            return "redirect:/admin/orders/add";
        }
    }

    @GetMapping("/orders/view/{id}")
    public String viewOrder(@PathVariable Long id, HttpSession session, Model model, RedirectAttributes ra) {
        if (!isAdmin(session)) return "redirect:/login";

        try {
            Order order = orderService.getOrderById(id);
            model.addAttribute("order", order);
            return "admin/order-details";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Order not found!");
            return "redirect:/admin/orders";
        }
    }

    @PostMapping("/orders/delete/{id}")
    public String deleteOrder(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        if (!isAdmin(session)) return "redirect:/login";

        try {
            orderService.deleteOrder(id);
            ra.addFlashAttribute("success", "Order deleted successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error deleting order: " + e.getMessage());
        }
        return "redirect:/admin/orders";
    }

// ==========================================
    //  سجل المعاملات (Transactions)
    // ==========================================
    @GetMapping("/transactions")
    public String listTransactions(HttpSession session, Model model,
                                   @RequestParam(required = false) String search,
                                   @RequestParam(required = false) String type) {
        if (!isAdmin(session)) return "redirect:/login";

        List<Transaction> transactions = transactionService.searchTransactions(search, type);
        
        model.addAttribute("transactions", transactions);
        model.addAttribute("search", search);
        model.addAttribute("type", type);

        // بيانات مسطحة للتصدير CSV (تجنب مشاكل التسلسل الدائري في JSON)
        List<Map<String, Object>> flatList = new ArrayList<>();
        for (Transaction t : transactions) {
            Map<String, Object> row = new HashMap<>();
            row.put("date", t.getTransactionDate() != null ? t.getTransactionDate().toString() : "");
            row.put("productName", t.getProduct() != null ? t.getProduct().getName() : "N/A");
            row.put("sku", t.getProduct() != null ? t.getProduct().getSku() : "N/A");
            row.put("type", t.getTransactionType() != null ? t.getTransactionType().name() : "N/A");
            row.put("quantity", t.getQuantity());
            row.put("username", t.getUser() != null ? t.getUser().getUsername() : "System");
            row.put("notes", t.getNotes() != null ? t.getNotes() : "");
            flatList.add(row);
        }
        model.addAttribute("transactionsJson", flatList);

        return "admin/transactions";
    }

// ==========================================
// إدارة الموردين (Suppliers)
// ==========================================


// 1. عرض قائمة الموردين مع دعم البحث
@GetMapping("/suppliers")
public String listSuppliers(HttpSession session, Model model, 
                           @RequestParam(required = false) String search) {
    if (!isAdmin(session)) return "redirect:/login";

    List<Supplier> suppliers;
    if (search != null && !search.trim().isEmpty()) {
        suppliers = supplierService.searchSuppliers(search.trim());
    } else {
        suppliers = supplierService.getAllSuppliers();
    }

    model.addAttribute("suppliers", suppliers);
    model.addAttribute("search", search);
    return "admin/suppliers";
}

// 2. عرض نموذج إضافة مورد جديد
@GetMapping("/suppliers/add")
public String showAddSupplierForm(HttpSession session, Model model) {
    if (!isAdmin(session)) return "redirect:/login";
    
    model.addAttribute("supplier", new Supplier());
    return "admin/add-supplier";
}

// 3. معالجة إضافة مورد جديد
@PostMapping("/suppliers/add")
public String addSupplier(@Valid @ModelAttribute("supplier") Supplier supplier, 
                         BindingResult result, HttpSession session, RedirectAttributes ra) {
    if (!isAdmin(session)) return "redirect:/login";
    if (result.hasErrors()) {
        return "admin/add-supplier";
    }

    try {
        supplierService.createSupplier(supplier);
        ra.addFlashAttribute("success", "تم إضافة المورد بنجاح!");
    } catch (IllegalArgumentException e) {
        // في حال كان الاسم مكرراً كما برمجنا في الـ Service
        ra.addFlashAttribute("error", e.getMessage());
        return "redirect:/admin/suppliers/add";
    }
    
    return "redirect:/admin/suppliers";
}

// 4. عرض نموذج تعديل مورد موجود
@GetMapping("/suppliers/edit/{id}")
public String showEditSupplierForm(@PathVariable Long id, HttpSession session, Model model, RedirectAttributes ra) {
    if (!isAdmin(session)) return "redirect:/login";

    try {
        Supplier supplier = supplierService.getSupplierById(id);
        model.addAttribute("supplier", supplier);
        return "admin/add-supplier"; // نستخدم نفس الـ HTML للإضافة والتعديل
    } catch (Exception e) {
        ra.addFlashAttribute("error", "المورد غير موجود!");
        return "redirect:/admin/suppliers";
    }
}

// 5. معالجة تحديث بيانات المورد
@PostMapping("/suppliers/edit/{id}")
public String updateSupplier(@PathVariable Long id, @Valid @ModelAttribute("supplier") Supplier supplier, 
                            BindingResult result, HttpSession session, RedirectAttributes ra) {
    if (!isAdmin(session)) return "redirect:/login";
    if (result.hasErrors()) {
        return "admin/add-supplier";
    }

    try {
        supplierService.updateSupplier(id, supplier);
        ra.addFlashAttribute("success", "تم تحديث بيانات المورد بنجاح!");
    } catch (IllegalArgumentException e) {
        ra.addFlashAttribute("error", e.getMessage());
        return "redirect:/admin/suppliers/edit/" + id;
    }

    return "redirect:/admin/suppliers";
}

// 6. حذف مورد
@PostMapping("/suppliers/delete/{id}")
public String deleteSupplier(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
    if (!isAdmin(session)) return "redirect:/login";

    try {
        supplierService.deleteSupplier(id);
        ra.addFlashAttribute("success", "تم حذف المورد بنجاح!");
    } catch (IllegalArgumentException e) {
        // سيظهر هذا الخطأ إذا كان المورد مرتبطاً بمنتجات (المنطق الذي كتبناه في الـ Service)
        ra.addFlashAttribute("error", e.getMessage());
    } catch (Exception e) {
        ra.addFlashAttribute("error", "حدث خطأ أثناء الحذف!");
    }
    
    return "redirect:/admin/suppliers";
}

// ==========================================
// إدارة الفئات (Categories)
// ==========================================

// 1. عرض قائمة الفئات مع دعم البحث وإحصائيات بسيطة
@GetMapping("/categories")
public String listCategories(HttpSession session, Model model, 
                            @RequestParam(required = false) String search) {
    if (!isAdmin(session)) return "redirect:/login";

    List<Category> categories;
    if (search != null && !search.trim().isEmpty()) {
        categories = categoryService.searchCategories(search.trim());
    } else {
        categories = categoryService.getAllCategories();
    }

    // حساب إحصائيات سريعة للبطاقات (Stats)
    long totalProducts = categories.stream()
            .mapToLong(c -> c.getProducts() != null ? c.getProducts().size() : 0).sum();
    long emptyCategories = categories.stream()
            .filter(c -> c.getProducts() == null || c.getProducts().isEmpty()).count();

    model.addAttribute("categories", categories);
    model.addAttribute("totalProducts", totalProducts);
    model.addAttribute("emptyCategories", emptyCategories);
    model.addAttribute("search", search);
    
    return "admin/categories";
}

// 2. عرض نموذج إضافة فئة جديدة
@GetMapping("/categories/add")
public String showAddCategoryForm(HttpSession session, Model model) {
    if (!isAdmin(session)) return "redirect:/login";
    
    model.addAttribute("category", new Category());
    return "admin/add-category";
}

// 3. معالجة إضافة فئة جديدة
@PostMapping("/categories/add")
public String addCategory(@Valid @ModelAttribute("category") Category category, 
                          BindingResult result, HttpSession session, RedirectAttributes ra) {
    if (!isAdmin(session)) return "redirect:/login";
    if (result.hasErrors()) {
        return "admin/add-category";
    }

    try {
        categoryService.createCategory(category);
        ra.addFlashAttribute("success", "تم إنشاء الفئة بنجاح!");
    } catch (IllegalArgumentException e) {
        ra.addFlashAttribute("error", e.getMessage());
        return "redirect:/admin/categories/add";
    }
    
    return "redirect:/admin/categories";
}

// 4. عرض نموذج تعديل فئة
@GetMapping("/categories/edit/{id}")
public String showEditCategoryForm(@PathVariable Long id, HttpSession session, Model model, RedirectAttributes ra) {
    if (!isAdmin(session)) return "redirect:/login";

    try {
        Category category = categoryService.getCategoryById(id);
        model.addAttribute("category", category);
        return "admin/add-category";
    } catch (Exception e) {
        ra.addFlashAttribute("error", "الفئة غير موجودة!");
        return "redirect:/admin/categories";
    }
}

// 5. حذف فئة (مع معالجة خطأ وجود منتجات)
@PostMapping("/categories/delete/{id}")
public String deleteCategory(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
    if (!isAdmin(session)) return "redirect:/login";

    try {
        categoryService.deleteCategory(id);
        ra.addFlashAttribute("success", "تم حذف الفئة بنجاح!");
    } catch (IllegalArgumentException e) {
        // هذا الخطأ سيظهر إذا كانت الفئة تحتوي على منتجات كما برمجنا في الـ Service
        ra.addFlashAttribute("error", e.getMessage());
    } catch (Exception e) {
        ra.addFlashAttribute("error", "حدث خطأ غير متوقع أثناء الحذف!");
    }
    
    return "redirect:/admin/categories";
}



}