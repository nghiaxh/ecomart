package com.ecomart.dto.response;

import java.time.Instant;
import java.util.List;

public record ChatSessionResponse(
        Long id,
        String title,
        Instant createdAt,
        List<ChatResponse.MessageResponse> messages
) {
}
