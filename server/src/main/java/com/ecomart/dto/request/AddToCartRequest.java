package com.ecomart.dto.request;

import jakarta.validation.constraints.NotNull;

public record AddToCartRequest(
        @NotNull Long productId,
        @NotNull Integer quantity
) {
}
