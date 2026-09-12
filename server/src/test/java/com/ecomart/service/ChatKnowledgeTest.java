package com.ecomart.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatKnowledgeTest {

    @Test
    void loadsKnowledgeJsonFromClasspath() {
        ChatKnowledge knowledge = new ChatKnowledge(new ObjectMapper());

        assertFalse(knowledge.entries().isEmpty());
        assertTrue(knowledge.entries().size() >= 30);
        assertTrue(knowledge.entries().stream()
                .allMatch(e -> e.id() != null && !e.id().isBlank()
                        && e.category() != null && !e.category().isBlank()
                        && !e.keywords().isEmpty()
                        && e.answer() != null && !e.answer().isBlank()));
    }
}