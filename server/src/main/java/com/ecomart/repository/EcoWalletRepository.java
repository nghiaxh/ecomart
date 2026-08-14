package com.ecomart.repository;

import com.ecomart.domain.entity.EcoWallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EcoWalletRepository extends JpaRepository<EcoWallet, Long> {
    Optional<EcoWallet> findByCustomerId(Long customerId);
}
