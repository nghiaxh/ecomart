package com.ecomart.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoryRequest(
        Long parentId,
        @NotBlank String name,
        @NotBlank String slug,
        String icon,
        Integer displayOrder,
        @NotNull Boolean active) {
}
