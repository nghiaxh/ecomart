package com.ecomart.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CheckoutRequest(
        @NotNull Long addressId,
        @NotBlank String paymentMethod,
        String notes
) {
}
