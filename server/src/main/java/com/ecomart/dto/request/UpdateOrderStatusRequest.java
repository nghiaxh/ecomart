package com.ecomart.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull String status
) {
}
