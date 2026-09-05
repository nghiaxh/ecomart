package com.ecomart.dto.response;

import java.util.List;

public record CartResponse(
        List<CartItemResponse> items,
        double subtotal,
        int itemCount
) {
    public record CartItemResponse(
            Long productId,
            String productName,
            String productSlug,
            String imageUrl,
            double price,
            int quantity,
            int stock
    ) {}
}
