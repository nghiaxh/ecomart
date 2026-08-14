package com.ecomart.dto.request;

public record ProfileUpdateRequest(
        String username,
        String numberPhone,
        String avatarUrl,
        String currentPassword,
        String newPassword
) {
}
