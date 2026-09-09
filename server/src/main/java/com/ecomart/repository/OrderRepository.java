package com.ecomart.repository;

import com.ecomart.domain.entity.Order;
import com.ecomart.domain.enums.OrderStatus;
import com.ecomart.domain.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Override
    @EntityGraph(attributePaths = {"items", "items.product", "items.product.images", "payment", "customer"})
    Optional<Order> findById(Long id);

    @EntityGraph(attributePaths = {"items", "items.product", "items.product.images", "payment", "customer"})
    Page<Order> findByCustomerId(Long customerId, Pageable pageable);

    @EntityGraph(attributePaths = {"items", "items.product", "items.product.images", "payment", "customer"})
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"items", "items.product", "items.product.images", "payment", "customer"})
    Page<Order> findAll(Pageable pageable);

    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o " +
            "WHERE o.status = :completed OR o.payment.status = :paid")
    Long sumTotalByStatusAndPaid(@Param("completed") OrderStatus completed,
                                 @Param("paid") PaymentStatus paid);

    long countByCustomerId(Long customerId);

    @Query("SELECT o.createdAt AS createdAt, o.total AS total, o.status AS status, o.payment.status AS paymentStatus " +
            "FROM Order o " +
            "WHERE o.createdAt >= :since")
    List<OrderStatRow> orderStatsSince(@Param("since") Instant since);

    @Query("SELECT o.status AS status, COUNT(o) AS count FROM Order o GROUP BY o.status")
    List<StatusCountRow> countByStatus();

    @Query("SELECT oi.product.id AS productId, " +
            "COALESCE(MAX(oi.productNameSnapshot), '') AS name, " +
            "SUM(oi.quantity) AS quantity, " +
            "SUM(oi.quantity * oi.unitPrice) AS revenue " +
            "FROM OrderItem oi " +
            "GROUP BY oi.product.id " +
            "ORDER BY quantity DESC")
    List<TopProductRow> topProducts(Pageable pageable);

    interface OrderStatRow {
        Instant getCreatedAt();

        double getTotal();

        OrderStatus getStatus();

        PaymentStatus getPaymentStatus();
    }

    interface StatusCountRow {
        OrderStatus getStatus();

        long getCount();
    }

    interface TopProductRow {
        Long getProductId();

        String getName();

        long getQuantity();

        double getRevenue();
    }
}
