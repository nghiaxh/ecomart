package com.ecomart.dto.response;

import com.ecomart.domain.enums.OrderStatus;
import com.ecomart.domain.enums.PaymentMethod;
import com.ecomart.domain.enums.PaymentStatus;

import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        String receiverName,
        String receiverPhone,
        String address,
        OrderStatus status,
        double subtotal,
        double shippingFee,
        double total,
        int ecoPointsEarned,
        String notes,
        Instant createdAt,
        PaymentResponse payment,
        List<OrderItemResponse> items
) {
    public record PaymentResponse(
            PaymentMethod method,
            PaymentStatus status,
            double amount,
            String payosOrderCode,
            Instant paidAt
    ) {}

    public record OrderItemResponse(
            Long productId,
            String productName,
            String imageUrl,
            double unitPrice,
            int quantity,
            double unitCo2Saved
    ) {}
}
