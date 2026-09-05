package com.ecomart.repository;

import com.ecomart.domain.entity.OrderItem;
import com.ecomart.domain.entity.OrderItemId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemId> {
}
