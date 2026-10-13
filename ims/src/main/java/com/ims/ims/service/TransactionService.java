package com.ims.ims.service;

import com.ims.ims.model.Transaction;
import com.ims.ims.model.TransactionType;
import com.ims.ims.model.Product;
import com.ims.ims.repository.TransactionRepository;
import com.ims.ims.repository.ProductRepository;
import com.ims.ims.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ProductRepository productRepository;

    // نسبة الضريبة الثابتة (10%) كما طلبت
    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 1. استرجاع البيانات الأساسية (Data Retrieval)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Transactional(readOnly = true)
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public long countSales() {
        return transactionRepository.findAll().stream()
                .filter(t -> t.getTransactionType() == TransactionType.SALE).count();
    }

    @Transactional(readOnly = true)
    public long countPurchases() {
        return transactionRepository.findAll().stream()
                .filter(t -> t.getTransactionType() == TransactionType.PURCHASE).count();
    }

    /**
     * جلب بيانات المبيعات موزعة حسب الفئة (للـ Donut Chart)
     */
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getCategoryRevenueData() {
        return transactionRepository.findAll().stream()
                .filter(t -> t.getTransactionType() == TransactionType.SALE)
                .filter(t -> t.getProduct().getCategory() != null)
                .collect(Collectors.groupingBy(
                    t -> t.getProduct().getCategory().getName(),
                    Collectors.mapping(
                        t -> {
                            BigDecimal price = (t.getProduct().getSellPrice() != null)
                                    ? t.getProduct().getSellPrice() : BigDecimal.ZERO;
                            return price.multiply(new BigDecimal(t.getQuantity()));
                        },
                        Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                    )
                ));
    }

    

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 2. المحرك المالي (Financial Engine)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * إجمالي الإيرادات = مجموع (الكمية × سعر البيع) لكل مبيعة
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalRevenue() {
        return transactionRepository.findAll().stream()
                .filter(t -> t.getTransactionType() == TransactionType.SALE)
                .map(t -> {
                    BigDecimal price = (t.getProduct().getSellPrice() != null) ? 
                                       t.getProduct().getSellPrice() : BigDecimal.ZERO;
                    return price.multiply(new BigDecimal(t.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * حساب قيمة الضريبة المستحقة (10% من إجمالي الإيرادات)
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTaxAmount() {
        return calculateTotalRevenue().multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * إجمالي تكاليف المشتريات = مجموع (الكمية × سعر التكلفة)
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalCosts() {
        return transactionRepository.findAll().stream()
                .filter(t -> t.getTransactionType() == TransactionType.PURCHASE)
                .map(t -> {
                    BigDecimal cost = (t.getProduct().getCostPrice() != null) ? 
                                      t.getProduct().getCostPrice() : BigDecimal.ZERO;
                    return cost.multiply(new BigDecimal(t.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * حساب صافي الربح الإجمالي (قبل الضريبة)
     * المعادلة: إجمالي المبيعات - إجمالي المشتريات
     * ملاحظة: الكنترولر يتولى خصم الضريبة مرة واحدة فقط
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateNetProfit() {
        return calculateTotalRevenue().subtract(calculateTotalCosts());
    }

    /**
     * حساب نسبة هامش الربح (Profit Margin %)
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateProfitMargin() {
        BigDecimal revenue = calculateTotalRevenue();
        if (revenue.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;

        return calculateNetProfit().divide(revenue, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100))
                .setScale(1, RoundingMode.HALF_UP);
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 3. البحث، الرسوم البيانية وإدارة المخزون
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * البحث المتقدم في العمليات حسب اسم المنتج، SKU، أو النوع
     */
    @Transactional(readOnly = true)
    public List<Transaction> searchTransactions(String search, String type) {
        List<Transaction> transactions;
        if (search != null && !search.trim().isEmpty()) {
            transactions = transactionRepository.findByProduct_NameContainingIgnoreCaseOrProduct_SkuContainingIgnoreCase(search, search);
        } else {
            transactions = transactionRepository.findAll();
        }

        if (type != null && !type.isEmpty() && !type.equalsIgnoreCase("ALL")) {
            try {
                TransactionType filterType = TransactionType.valueOf(type.toUpperCase());
                transactions = transactions.stream()
                        .filter(t -> t.getTransactionType() == filterType)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                // تجاهل الفلترة في حال وجود نوع غير صالح
            }
        }

        return transactions.stream()
                .sorted((t1, t2) -> t2.getTransactionDate().compareTo(t1.getTransactionDate()))
                .collect(Collectors.toList());
    }

    /**
     * جلب بيانات المبيعات مفلترة حسب الفترة الزمنية (للـ Sales Trend Chart)
     */
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getDailySalesData(String period) {
        LocalDateTime startDate = getStartDateForPeriod(period);
        return transactionRepository.findAll().stream()
                .filter(t -> t.getTransactionType() == TransactionType.SALE)
                .filter(t -> t.getTransactionDate().isAfter(startDate))
                .collect(Collectors.groupingBy(
                    t -> t.getTransactionDate().toLocalDate().toString(),
                    TreeMap::new,
                    Collectors.mapping(
                        t -> {
                            BigDecimal price = (t.getProduct().getSellPrice() != null)
                                    ? t.getProduct().getSellPrice() : BigDecimal.ZERO;
                            return price.multiply(new BigDecimal(t.getQuantity()));
                        },
                        Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                    )
                ));
    }

    /**
     * جلب بيانات المبيعات لآخر 30 يوم (للـ Sales Trend Chart) - الإصدار الافتراضي
     */
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getDailySalesData() {
        return getDailySalesData("month");
    }

    /**
     * حساب صافي الربح لكل فئة (Category Net Profit)
     * المعادلة: Σ (sellPrice - costPrice) × quantity لكل SALE مجمّعة حسب الفئة
     */
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getCategoryNetProfitData() {
        return transactionRepository.findAll().stream()
                .filter(t -> t.getTransactionType() == TransactionType.SALE)
                .filter(t -> t.getProduct().getCategory() != null)
                .collect(Collectors.groupingBy(
                    t -> t.getProduct().getCategory().getName(),
                    Collectors.mapping(
                        t -> {
                            BigDecimal sell = (t.getProduct().getSellPrice() != null)
                                    ? t.getProduct().getSellPrice() : BigDecimal.ZERO;
                            BigDecimal cost = (t.getProduct().getCostPrice() != null)
                                    ? t.getProduct().getCostPrice() : BigDecimal.ZERO;
                            BigDecimal unitProfit = sell.subtract(cost);
                            return unitProfit.multiply(new BigDecimal(t.getQuantity()));
                        },
                        Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                    )
                ));
    }

    /**
     * تحديد تاريخ البداية بناءً على الفترة الزمنية المختارة
     */
    private LocalDateTime getStartDateForPeriod(String period) {
        if (period == null) period = "month";
        return switch (period.toLowerCase()) {
            case "today" -> LocalDate.now().atStartOfDay();
            case "week" -> LocalDateTime.now().minusDays(7);
            case "year" -> LocalDateTime.now().minusYears(1);
            default -> LocalDateTime.now().minusDays(30); // month
        };
    }

    /**
     * حساب إجمالي مبيعات اليوم الحالي
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateDailySales() {
        LocalDate today = LocalDate.now();
        return transactionRepository.findAll().stream()
                .filter(t -> t.getTransactionType() == TransactionType.SALE)
                .filter(t -> t.getTransactionDate().toLocalDate().equals(today))
                .map(t -> {
                    BigDecimal price = (t.getProduct().getSellPrice() != null)
                            ? t.getProduct().getSellPrice() : BigDecimal.ZERO;
                    return price.multiply(new BigDecimal(t.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
         * جلب جميع العمليات التي قام بها مستخدم (موظف) معين
         * مرتبة تنازلياً حسب التاريخ (من الأحدث إلى الأقدم)
         */
        @Transactional(readOnly = true)
        public List<Transaction> getUserTransactions(Long userId) {
            // التحقق من صحة المعرف ثم جلب القائمة من المستودع
            return transactionRepository.findByUserIdOrderByTransactionDateDesc(userId);
        }

    @Transactional(readOnly = true)
        public long countAllTransactions() {
            // استخدام ميثود count() الجاهزة في JpaRepository
            return transactionRepository.count(); 
        }

    /**
     * إنشاء عملية جديدة وتحديث كمية المخزون (Core Logic)
     */
    public Transaction createTransaction(Transaction transaction) {
        Product product = productRepository.findById(transaction.getProduct().getId())
                .orElseThrow(() -> new ResourceNotFoundException("المنتج غير موجود"));

        int currentStock = product.getStockQuantity();
        int transactionQty = transaction.getQuantity();

        // حفظ الكمية قبل التغيير (للتتبع)
        transaction.setPreviousQuantity(currentStock);

        // تحديث المخزون بناءً على نوع العملية
        switch (transaction.getTransactionType()) {
            case SALE:
            case WITHDRAWAL:
                if (currentStock < transactionQty) {
                    throw new IllegalArgumentException("المخزون لا يكفي! المتبقي: " + currentStock);
                }
                product.setStockQuantity(currentStock - transactionQty);
                break;
            case PURCHASE:
            case ADDITION:
            case RETURN:
                product.setStockQuantity(currentStock + transactionQty);
                break;
            case ADJUSTMENT:
                // التسوية: تعيين الكمية مباشرة
                product.setStockQuantity(transactionQty);
                break;
        }

        // حفظ الكمية بعد التغيير (للتتبع)
        transaction.setNewQuantity(product.getStockQuantity());

        productRepository.save(product); // تحديث جدول المنتجات
        return transactionRepository.save(transaction); // حفظ العملية
    }

    public void deleteTransaction(Long id) {
        transactionRepository.deleteById(id);
    }
}