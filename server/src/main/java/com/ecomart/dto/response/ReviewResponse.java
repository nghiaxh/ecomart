package com.ecomart.dto.response;

import java.time.Instant;

public record ReviewResponse(
        Long id,
        Long customerId,
        String customerName,
        int rating,
        String content,
        boolean hidden,
        Instant createdAt
) {
}
