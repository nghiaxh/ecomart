package com.ecomart.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ProductRequest(
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Size(max = 200) String slug,
        @Size(max = 4000) String description,
        @NotNull @Positive Double price,
        @NotNull @PositiveOrZero Integer stock,
        Double weight,
        @Size(max = 100) String origin,
        @NotNull Long categoryId,
        @NotNull Boolean active,
        List<@Valid ProductImageRequest> images,
        List<@Valid ProductMaterialRequest> materials) {

    public record ProductImageRequest(
            @NotBlank @Pattern(regexp = "^(https?://|/).*", message = "Đường dẫn ảnh không hợp lệ") String url,
            boolean primary,
            Integer displayOrder) {}

    public record ProductMaterialRequest(Long materialId, Double percentage) {}
}
