package com.ecomart.dto.response;

import com.ecomart.domain.enums.UserRole;

public record AuthResponse(
        String token,
        Long id,
        String username,
        String email,
        String numberPhone,
        String avatarUrl,
        UserRole role
) {
}
