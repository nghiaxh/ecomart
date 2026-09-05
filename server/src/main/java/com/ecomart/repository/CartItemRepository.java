package com.ecomart.repository;

import com.ecomart.domain.entity.CartItem;
import com.ecomart.domain.entity.CartItemId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, CartItemId> {
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
    void deleteByCartId(Long cartId);
}
