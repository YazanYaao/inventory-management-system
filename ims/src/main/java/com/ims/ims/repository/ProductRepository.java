package com.ims.ims.repository;

import com.ims.ims.model.Product;
import com.ims.ims.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;
import java.math.BigDecimal;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // الاستعلامات المخصصة (Derived Queries)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    Optional<Product> findBySku(String sku);

    // سيعمل تلقائياً لأن الحقل في Category هو id
    List<Product> findByCategoryId(Long categoryId);

  

    // سيعمل تلقائياً لأن الحقل في Supplier هو id
    List<Product> findBySupplierId(Long supplierId);

    List<Product> findByCategory(Category category);

    List<Product> findByNameContainingIgnoreCase(String name);

    boolean existsBySku(String sku);

    long countByCategory(Category category);

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // الاستعلامات المتقدمة (JPQL Queries) - تم تحديث المسارات لـ .id
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId")
    List<Product> findProductsByCategory(@Param("categoryId") Long categoryId);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity < :threshold ORDER BY p.stockQuantity ASC")
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity = 0")
    List<Product> findOutOfStockProducts();

    @Query("SELECT p FROM Product p WHERE p.sellPrice BETWEEN :minPrice AND :maxPrice")
    List<Product> findByPriceRange(
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice
    );


    /**
     * تم تحديث المسارات هنا من .categoryId و .supplierId إلى .id
     */
    @Query("SELECT p FROM Product p WHERE " +
           "(:name IS NULL OR p.name LIKE CONCAT('%', :name, '%')) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:supplierId IS NULL OR p.supplier.id = :supplierId)")
    List<Product> findByMultipleCriteria(
        @Param("name") String name,
        @Param("categoryId") Long categoryId,
        @Param("supplierId") Long supplierId
    );

    @Query("SELECT p FROM Product p " +
           "ORDER BY (p.sellPrice - p.costPrice) * p.stockQuantity DESC")
    List<Product> findTopSellingProducts();

    
    
    

    
}