package com.ims.ims.repository;
 
import com.ims.ims.model.Transaction;
import com.ims.ims.model.TransactionType;
import com.ims.ims.model.Product;
import com.ims.ims.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDateTime;
 
/**
 * Repository لإدارة بيانات المعاملات
 * 
 * هذا Repository مهم جداً لتتبع جميع العمليات على المخزون
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    /**
     * الحصول على جميع المعاملات لمنتج معين
     * SELECT * FROM transactions WHERE product_id = ?
     * 
     * @param product - المنتج
     * @return قائمة بجميع معاملات المنتج
     */
    List<Transaction> findByProduct(Product product);

    List<Transaction> findByUserIdOrderByTransactionDateDesc(Long userId);
    
    /**
     * الحصول على جميع المعاملات لمستخدم معين
     * SELECT * FROM transactions WHERE user_id = ?
     * 
     * @param user - المستخدم
     * @return قائمة بجميع معاملات المستخدم
     */
    List<Transaction> findByUser(User user);
    
    /**
     * الحصول على جميع معاملات نوع معين
     * SELECT * FROM transactions WHERE transaction_type = ?
     * 
     * @param transactionType - نوع المعاملة (PURCHASE, SALE, إلخ)
     * @return قائمة بجميع المعاملات من هذا النوع
     */
    List<Transaction> findByTransactionType(TransactionType transactionType);
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // استعلامات JPQL (تم تحديث المسار من .productId إلى .id)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Query("SELECT t FROM Transaction t WHERE t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<Transaction> findByTransactionDateBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * تحديث المسار: t.product.id
     */
    @Query("SELECT t FROM Transaction t WHERE t.product.id = :productId AND " +
           "t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<Transaction> findProductTransactionsByDateRange(
        @Param("productId") Long productId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * تحديث المسار: t.product.id
     */
    @Query("SELECT t FROM Transaction t WHERE t.product.id = :productId " +
           "ORDER BY t.transactionDate DESC LIMIT 1")
    List<Transaction> findLatestTransactionForProduct(@Param("productId") Long productId);
    
    long countByTransactionType(TransactionType transactionType);
    
    List<Transaction> findByNotesContainingIgnoreCase(String notes);
    
    /**
     * تحديث المسار: t.product.id لعمليات الجمع (SALE)
     */
    @Query("SELECT COALESCE(SUM(t.quantity), 0) FROM Transaction t " +
           "WHERE t.product.id = :productId AND t.transactionType = 'SALE'")
    Integer getTotalSalesQuantity(@Param("productId") Long productId);
    
    /**
     * تحديث المسار: t.product.id لعمليات الجمع (PURCHASE)
     */
    @Query("SELECT COALESCE(SUM(t.quantity), 0) FROM Transaction t " +
           "WHERE t.product.id = :productId AND t.transactionType = 'PURCHASE'")
    Integer getTotalPurchaseQuantity(@Param("productId") Long productId);

    // هذا هو السطر الذي يربط الكنترولر بقاعدة البيانات
    List<Transaction> findByProduct_NameContainingIgnoreCaseOrProduct_SkuContainingIgnoreCase(String name, String sku);

}