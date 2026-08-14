package com.ecomart.dto.response;

import com.ecomart.domain.enums.NotificationType;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        String title,
        String message,
        NotificationType type,
        boolean read,
        Instant createdAt
) {
}
