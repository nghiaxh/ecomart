package com.ecomart.dto.response;

import com.ecomart.domain.enums.UserRole;

import java.time.Instant;
import java.time.LocalDate;

public record UserSummaryResponse(
        Long id,
        String username,
        String email,
        String numberPhone,
        String avatarUrl,
        UserRole role,
        boolean isActive,
        Instant createdAt,
        LocalDate hireDate
) {
}
