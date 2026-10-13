package com.ims.ims.repository;

import com.ims.ims.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findBySupplierId(Long supplierId);

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findAllByOrderByOrderDateDesc();

    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    long countByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Aggregates order count grouped by year and month within the specified date range.
     * Returns: List of Object arrays where [0]=year, [1]=month, [2]=count
     */
    @Query("""
            SELECT
            YEAR(o.orderDate) as year,
            MONTH(o.orderDate) as month,
            COUNT(o) as count
            FROM Order o
            WHERE o.orderDate BETWEEN :startDate AND :endDate
            GROUP BY YEAR(o.orderDate), MONTH(o.orderDate)
            ORDER BY YEAR(o.orderDate), MONTH(o.orderDate)
            """)
    List<Object[]> countOrdersGroupedByMonth(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
