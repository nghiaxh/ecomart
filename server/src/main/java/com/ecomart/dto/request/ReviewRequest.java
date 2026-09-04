package com.ecomart.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewRequest(
        @NotNull Long productId,
        @Min(1) @Max(5) int rating,
        @NotNull @Size(max = 1000) String content
) {
}
