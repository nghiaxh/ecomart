package com.ecomart.dto.request;

import java.util.List;

public record ChatRequest(List<ChatRequest.Message> messages) {

    public record Message(String role, String content) {
    }
}