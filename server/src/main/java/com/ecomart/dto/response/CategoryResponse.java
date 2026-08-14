package com.ecomart.dto.response;

import java.util.ArrayList;
import java.util.List;

public record CategoryResponse(
        Long id,
        String name,
        String slug,
        String icon,
        int displayOrder,
        boolean active,
        List<CategoryResponse> children
) {
    public static CategoryResponse ofRoot(Long id, String name, String slug, String icon, int displayOrder, boolean active) {
        return new CategoryResponse(id, name, slug, icon, displayOrder, active, new ArrayList<>());
    }
}
