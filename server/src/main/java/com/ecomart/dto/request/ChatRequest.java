package com.ecomart.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChatRequest(
        @NotBlank String message,
        Long sessionId
) {
}
