package com.ecomart.service;

import com.ecomart.common.SecurityUtils;
import com.ecomart.domain.entity.Customer;
import com.ecomart.domain.entity.EcoWallet;
import com.ecomart.domain.entity.PointTransaction;
import com.ecomart.domain.enums.PointTransactionType;
import com.ecomart.dto.response.EcoWalletResponse;
import com.ecomart.exception.ResourceNotFoundException;
import com.ecomart.repository.EcoWalletRepository;
import com.ecomart.repository.PointTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EcoWalletService {

    private final SecurityUtils securityUtils;
    private final EcoWalletRepository walletRepository;
    private final PointTransactionRepository transactionRepository;

    public EcoWalletService(SecurityUtils securityUtils,
                            EcoWalletRepository walletRepository,
                            PointTransactionRepository transactionRepository) {
        this.securityUtils = securityUtils;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    public EcoWalletResponse getMyWallet() {
        Customer customer = (Customer) securityUtils.currentUser();
        EcoWallet wallet = walletRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví Eco"));
        List<EcoWalletResponse.TransactionResponse> transactions =
                transactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId()).stream()
                        .map(t -> new EcoWalletResponse.TransactionResponse(t.getId(), t.getType(), t.getAmount(),
                                t.getDescription(), t.getCreatedAt()))
                        .toList();
        return new EcoWalletResponse(wallet.getBalance(), wallet.getTotalEarned(), transactions);
    }

    @Transactional
    public void record(Customer customer, PointTransactionType type, int amount, String description) {
        EcoWallet wallet = walletRepository.findByCustomerId(customer.getId())
                .orElseGet(() -> {
                    EcoWallet w = new EcoWallet();
                    w.setCustomer(customer);
                    return walletRepository.save(w);
                });
        PointTransaction tx = new PointTransaction();
        tx.setWallet(wallet);
        tx.setType(type);
        tx.setAmount(amount);
        tx.setDescription(description);
        transactionRepository.save(tx);
    }
}
