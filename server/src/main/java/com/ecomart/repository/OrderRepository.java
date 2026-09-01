package com.ecomart.repository;

import com.ecomart.domain.entity.Order;
import com.ecomart.domain.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByCustomerId(Long customerId, Pageable pageable);
    List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
}
