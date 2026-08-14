package com.ecomart.dto.response;

import com.ecomart.domain.enums.ChatRole;

import java.time.Instant;
import java.util.List;

public record ChatResponse(
        String botMessage,
        Long sessionId,
        List<MessageResponse> messages
) {
    public record MessageResponse(Long id, ChatRole role, String content, Instant createdAt) {}
}
