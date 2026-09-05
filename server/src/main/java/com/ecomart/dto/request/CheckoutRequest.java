package com.ecomart.dto.request;

import com.ecomart.domain.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CheckoutRequest(
        @NotNull Long addressId,
        @NotNull PaymentMethod paymentMethod,
        @Size(max = 500) String notes
) {
}
