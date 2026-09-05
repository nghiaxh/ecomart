package com.ecomart.dto.response;

import java.util.List;

public record ProductResponse(
        Long id,
        String name,
        String slug,
        String description,
        double price,
        int stock,
        double weight,
        String origin,
        Long categoryId,
        String categoryName,
        String categorySlug,
        boolean active,
        List<String> images,
        List<MaterialInfo> materials
) {
    public record MaterialInfo(Long id, String name, double percentage, String type) {}
}
