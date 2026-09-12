package com.ecomart.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChatKnowledge {

    private static final String RESOURCE = "data/chat-knowledge.json";

    private final List<ChatEntry> entries;

    public ChatKnowledge(ObjectMapper objectMapper) {
        try {
            List<ChatEntry> loaded = objectMapper.readValue(
                    new ClassPathResource(RESOURCE).getInputStream(),
                    new TypeReference<>() {
                    });
            this.entries = List.copyOf(loaded);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot load chat knowledge from " + RESOURCE, ex);
        }
    }

    public List<ChatEntry> entries() {
        return entries;
    }

    public record ChatEntry(String id, String category, List<String> keywords, String answer) {
    }
}