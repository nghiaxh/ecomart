package com.ecomart.repository;

import com.ecomart.domain.entity.OrderItem;
import com.ecomart.domain.entity.OrderItemId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemId> {
    List<OrderItem> findByOrderId(Long orderId);
}
