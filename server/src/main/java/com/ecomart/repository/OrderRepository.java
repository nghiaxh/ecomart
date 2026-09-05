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

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Override
    @EntityGraph(attributePaths = {"items", "items.product", "items.product.images", "payment", "customer"})
    Optional<Order> findById(Long id);

    @EntityGraph(attributePaths = {"items", "items.product", "items.product.images", "payment", "customer"})
    Page<Order> findByCustomerId(Long customerId, Pageable pageable);

    @EntityGraph(attributePaths = {"items", "items.product", "items.product.images", "payment", "customer"})
    List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    @EntityGraph(attributePaths = {"items", "items.product", "items.product.images", "payment", "customer"})
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"items", "items.product", "items.product.images", "payment", "customer"})
    Page<Order> findAll(Pageable pageable);

    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o " +
            "WHERE o.status = :completed OR o.payment.status = :paid")
    Long sumTotalByStatusAndPaid(@Param("completed") OrderStatus completed,
                                 @Param("paid") PaymentStatus paid);
}
