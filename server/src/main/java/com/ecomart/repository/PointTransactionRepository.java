package com.ecomart.repository;

import com.ecomart.domain.entity.PointTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {
    List<PointTransaction> findByWalletIdOrderByCreatedAtDesc(Long walletId);
}
