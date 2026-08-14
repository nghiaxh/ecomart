package com.ecomart.dto.response;

public record CheckoutResponse(
        Long orderId,
        String status,
        String payosCheckoutUrl,
        String message
) {
}
