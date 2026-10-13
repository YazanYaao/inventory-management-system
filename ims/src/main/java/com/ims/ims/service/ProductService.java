package com.ims.ims.service;

import com.ims.ims.model.Product;
import com.ims.ims.repository.ProductRepository;
import com.ims.ims.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ActivityLogService activityLogService;

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 1. عمليات القراءة الأساسية (Basic Read)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("المنتج غير موجود برقم: " + id));
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 2. عمليات التنبيه (Alert Operations)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Transactional(readOnly = true)
    public List<Product> getLowStockProducts(Integer threshold) {
        // يشمل المنتجات التي كميتها = 0 (نفذت) أو أقل من الحد الأدنى
        return productRepository.findAll().stream()
                .filter(p -> p.getStockQuantity() == null || p.getStockQuantity() <= threshold)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Product> getOutOfStockProducts() {
        return productRepository.findAll().stream()
                .filter(p -> p.getStockQuantity() == null || p.getStockQuantity() <= 0)
                .collect(Collectors.toList());
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 3. الحسابات المالية الاحترافية (Financial Core)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * حساب قيمة المخزون الإجمالية = Σ (الكمية * سعر التكلفة)
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalInventoryValue() {
        return productRepository.findAll().stream()
                .map(p -> {
                    BigDecimal cost = (p.getCostPrice() != null) ? p.getCostPrice() : BigDecimal.ZERO;
                    BigDecimal qty = new BigDecimal((p.getStockQuantity() != null) ? p.getStockQuantity() : 0);
                    return cost.multiply(qty);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

        /**
         * حساب إجمالي قيمة البيع للمخزون بالكامل
         * المعادلة: مجموع (الكمية × سعر البيع) لكل المنتجات
         */
        @Transactional(readOnly = true)
        public BigDecimal calculateTotalSellingValue() {
            return productRepository.findAll().stream()
                    .map(p -> {
                        BigDecimal sellPrice = (p.getSellPrice() != null) ? p.getSellPrice() : BigDecimal.ZERO;
                        BigDecimal qty = new BigDecimal((p.getStockQuantity() != null) ? p.getStockQuantity() : 0);
                        return sellPrice.multiply(qty);
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

    /**
     * حساب الأرباح المحتملة = Σ ((سعر البيع - سعر التكلفة) * الكمية)
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalProfit() {
        return productRepository.findAll().stream()
                .map(p -> {
                    BigDecimal cost = (p.getCostPrice() != null) ? p.getCostPrice() : BigDecimal.ZERO;
                    BigDecimal sell = (p.getSellPrice() != null) ? p.getSellPrice() : BigDecimal.ZERO;
                    BigDecimal qty = new BigDecimal((p.getStockQuantity() != null) ? p.getStockQuantity() : 0);
                    
                    BigDecimal unitProfit = sell.subtract(cost);
                    return unitProfit.multiply(qty);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * حساب نسبة الربح (Profit Margin) = (الأرباح / قيمة المخزون) * 100
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateProfitMargin() {
        BigDecimal totalProfit = calculateTotalProfit();
        BigDecimal inventoryValue = calculateTotalInventoryValue();

        if (inventoryValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        // الحساب مع تقريب لخانة واحدة بعد الفاصلة
        return totalProfit.divide(inventoryValue, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100))
                .setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * حساب متوسط سعر المنتج (Average Product Price)
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateAverageProductPrice() {
        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalPrices = products.stream()
                .map(p -> (p.getSellPrice() != null) ? p.getSellPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalPrices.divide(new BigDecimal(products.size()), 2, RoundingMode.HALF_UP);
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 4. إدارة البيانات (Write Operations)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    public Product createProduct(Product product) {
        if (product.getStockQuantity() == null) product.setStockQuantity(0);
        if (product.getCostPrice() == null) product.setCostPrice(BigDecimal.ZERO);
        if (product.getSellPrice() == null) product.setSellPrice(BigDecimal.ZERO);
        Product saved = productRepository.save(product);

        String supplierName = (saved.getSupplier() != null && saved.getSupplier().getName() != null)
                ? saved.getSupplier().getName() : "N/A";
        activityLogService.logActivity("PRODUCT", "New Product Created", "Product #" + saved.getId() + " for Supplier " + supplierName);

        return saved;
    }

    public Product addStock(Long productId, Integer quantity) {
        Product product = getProductById(productId); // جلب المنتج أو رمي Exception
        
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("يجب أن تكون الكمية المضافة أكبر من صفر");
        }

        int currentStock = (product.getStockQuantity() != null) ? product.getStockQuantity() : 0;
        product.setStockQuantity(currentStock + quantity);
        
        return productRepository.save(product);
    }

    /**
     * سحب كمية من المخزون الحالي مع التحقق من التوفر
     */
    public Product subtractStock(Long productId, Integer quantity) {
        Product product = getProductById(productId);
        
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("يجب أن تكون الكمية المسحوبة أكبر من صفر");
        }

        int currentStock = (product.getStockQuantity() != null) ? product.getStockQuantity() : 0;

        if (currentStock < quantity) {
            throw new IllegalArgumentException("المخزون غير كافٍ! المتوفر حالياً: " + currentStock);
        }

        product.setStockQuantity(currentStock - quantity);
        
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product details) {
        Product existing = getProductById(id);
        existing.setName(details.getName());
        existing.setCostPrice(details.getCostPrice());
        existing.setSellPrice(details.getSellPrice());
        existing.setStockQuantity(details.getStockQuantity());
        return productRepository.save(existing);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    // في ملف ProductService.java

            /**
             * البحث الديناميكي عن المنتجات
             * @param name: نص البحث (اسم المنتج)
             * @param categoryId: رقم الفئة (يمكن أن يكون null)
             * @param supplierId: رقم المورد (يمكن أن يكون null)
             */
            @Transactional(readOnly = true)
            public List<Product> searchProducts(String name, Long categoryId, Long supplierId) {
                // نتحقق إذا كان الاسم فارغاً تماماً لنرسل null بدلاً من نص فارغ للاستعلام
                String searchName = (name != null && !name.trim().isEmpty()) ? name.trim() : null;

                return productRepository.findByMultipleCriteria(searchName, categoryId, supplierId);
            }

    /**
     * Aggregates total stock quantity per category for the Distribution by Category Bar Chart
     * Matches reference ChartStats: returns [{ category: "laptop", quantity: 1 }, ...]
     */
    @Transactional(readOnly = true)
    public List<java.util.Map<String, Object>> getCategoryStockDistribution() {
        List<Product> products = productRepository.findAll();
        java.util.Map<String, Integer> categoryStock = products.stream()
                .filter(p -> p.getCategory() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getCategory().getName(),
                        Collectors.summingInt(p -> p.getStockQuantity() != null ? p.getStockQuantity() : 0)
                ));

        return categoryStock.entrySet().stream()
                .map(entry -> {
                    java.util.Map<String, Object> map = new java.util.HashMap<>();
                    map.put("category", entry.getKey());
                    map.put("quantity", entry.getValue());
                    return map;
                })
                .collect(Collectors.toList());
    }
}