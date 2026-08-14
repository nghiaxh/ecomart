package com.ecomart.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

public class ProductRequest {
    @NotBlank public String name;
    @NotBlank public String slug;
    public String description;
    @NotNull @Positive public Double price;
    @NotNull @PositiveOrZero public Integer stock;
    public Double carbonIndex;
    public Double baselineCarbonIndex;
    public Double ecoPointsPerUnit;
    public Double weight;
    public String origin;
    @NotNull public Long categoryId;
    @NotNull public Boolean active;
    public List<ProductImageRequest> images;
    public List<ProductMaterialRequest> materials;

    public record ProductImageRequest(String url, boolean primary, Integer displayOrder) {}

    public record ProductMaterialRequest(Long materialId, Double percentage) {}
}
