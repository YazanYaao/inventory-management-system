package com.ims.ims.controller;

import com.ims.ims.model.Transaction;
import com.ims.ims.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * REST Controller لإدارة سجل العمليات (Transactions)
 * يربط بين الـ Dashboard وبين البيانات المالية الحقيقية
 */
@RestController
@RequestMapping("/api/transactions")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Transaction Management", description = "REST APIs for inventory stock movements and audit trails")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 1. عمليات القراءة (Read Operations)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

@GetMapping("/count")
    public ResponseEntity<Long> getTransactionCount() {
        long count = transactionService.countAllTransactions();
        return ResponseEntity.ok(count);
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 2. المحرك المالي للرسوم البيانية (Financial Analytics)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * الحصول على ملخص مالي كامل (إيرادات، تكاليف، صافي ربح)
     * مفيد جداً لتحديث بيانات الـ Charts عبر JavaScript
     */
    @GetMapping("/stats/summary")
    public ResponseEntity<Map<String, BigDecimal>> getFinancialSummary() {
        return ResponseEntity.ok(Map.of(
            "totalRevenue", transactionService.calculateTotalRevenue(),
            "totalCosts", transactionService.calculateTotalCosts(),
            "netProfit", transactionService.calculateNetProfit()
        ));
    }

    @GetMapping("/stats/revenue")
    public ResponseEntity<BigDecimal> getTotalRevenue() {
        return ResponseEntity.ok(transactionService.calculateTotalRevenue());
    }

    @GetMapping("/stats/profit")
    public ResponseEntity<BigDecimal> getNetProfit() {
        return ResponseEntity.ok(transactionService.calculateNetProfit());
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 3. إدارة العمليات (Write Operations)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * إنشاء عملية جديدة (بيع، شراء، مرتجع)
     * ملاحظة: هذه العملية ستقوم بتحديث كمية المنتج تلقائياً في المخزون
     */
    @PostMapping
    public ResponseEntity<?> createTransaction(@RequestBody Transaction transaction) {
        try {
            Transaction created = transactionService.createTransaction(transaction);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // في حال كان المخزون غير كافٍ (للبيع)
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("خطأ في تنفيذ العملية");
        }
    }

    /**
     * حذف سجل عملية (تستخدم بحذر)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.ok("تم حذف السجل بنجاح");
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 4. إحصائيات نوع العمليات (Operation Breakdown)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @GetMapping("/stats/sales-count")
    public ResponseEntity<Long> getSalesCount() {
        return ResponseEntity.ok(transactionService.countSales());
    }

    @GetMapping("/stats/purchases-count")
    public ResponseEntity<Long> getPurchasesCount() {
        return ResponseEntity.ok(transactionService.countPurchases());
    }
}