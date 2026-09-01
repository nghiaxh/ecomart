package com.ecomart.dto.response;

import com.ecomart.domain.enums.UserRole;

import java.time.Instant;

public record ProfileResponse(
        Long id,
        String username,
        String email,
        String numberPhone,
        String avatarUrl,
        UserRole role,
        Instant createdAt
) {
}
