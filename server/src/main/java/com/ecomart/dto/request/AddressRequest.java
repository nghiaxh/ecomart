package com.ecomart.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddressRequest(
        @NotBlank String label,
        @NotBlank String street,
        @NotBlank String ward,
        @NotBlank String district,
        @NotBlank String city,
        @NotBlank String receiverName,
        @NotBlank String receiverPhone,
        boolean isDefault
) {
}
