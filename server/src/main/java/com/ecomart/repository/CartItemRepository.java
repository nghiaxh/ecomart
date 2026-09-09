package com.ecomart.repository;

import com.ecomart.domain.entity.CartItem;
import com.ecomart.domain.entity.CartItemId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, CartItemId> {
    void deleteByCartId(Long cartId);
}
