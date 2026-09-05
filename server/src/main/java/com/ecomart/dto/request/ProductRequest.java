package com.ecomart.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public class ProductRequest {
    @NotBlank @Size(max = 200) public String name;
    @NotBlank @Size(max = 200) public String slug;
    @Size(max = 4000) public String description;
    @NotNull @Positive public Double price;
    @NotNull @PositiveOrZero public Integer stock;
    public Double weight;
    @Size(max = 100) public String origin;
    @NotNull public Long categoryId;
    @NotNull public Boolean active;
    public List<@Valid ProductImageRequest> images;
    public List<@Valid ProductMaterialRequest> materials;

    public record ProductImageRequest(
            @NotBlank @Pattern(regexp = "^(https?://|/).*", message = "Đường dẫn ảnh không hợp lệ") String url,
            boolean primary,
            Integer displayOrder) {}

    public record ProductMaterialRequest(Long materialId, Double percentage) {}
}
