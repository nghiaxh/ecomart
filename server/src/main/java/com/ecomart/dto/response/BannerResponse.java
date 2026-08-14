package com.ecomart.dto.response;

public record BannerResponse(
        Long id,
        String title,
        String subtitle,
        String imageUrl,
        String linkUrl,
        int displayOrder,
        boolean active
) {
}
