package com.ecomart.dto.response;

import com.ecomart.domain.enums.PointTransactionType;

import java.time.Instant;
import java.util.List;

public record EcoWalletResponse(
        int balance,
        int totalEarned,
        List<TransactionResponse> transactions
) {
    public record TransactionResponse(
            Long id,
            PointTransactionType type,
            int amount,
            String description,
            Instant createdAt
    ) {}
}
