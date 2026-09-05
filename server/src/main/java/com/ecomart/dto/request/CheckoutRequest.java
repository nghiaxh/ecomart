package com.ecomart.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CheckoutRequest(
        @NotNull Long addressId,
        @NotBlank String paymentMethod,
        @Size(max = 500) String notes
) {
}
