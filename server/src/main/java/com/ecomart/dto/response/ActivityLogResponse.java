package com.ecomart.dto.response;

import java.time.Instant;

public record ActivityLogResponse(
        Long id,
        Long userId,
        String username,
        String role,
        String action,
        String entityType,
        Long entityId,
        String entityName,
        String detail,
        Instant createdAt
) {
}