package com.ecomart.repository;

import com.ecomart.domain.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductIdAndIsHiddenFalseOrderByCreatedAtDesc(Long productId);
    List<Review> findByProductIdOrderByCreatedAtDesc(Long productId);
    boolean existsByCustomerIdAndProductId(Long customerId, Long productId);
}
