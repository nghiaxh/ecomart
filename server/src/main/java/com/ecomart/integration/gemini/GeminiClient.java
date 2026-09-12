package com.ecomart.integration.gemini;

import com.ecomart.config.GeminiProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class GeminiClient {

    private static final String GENERATE_URL = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";

    private final GeminiProperties properties;
    private final RestTemplate restTemplate;

    public GeminiClient(GeminiProperties properties, RestTemplate restTemplate) {
        this.properties = properties;
        this.restTemplate = restTemplate;
    }

    /**
     * @return the generated text, or empty when no API key is configured or the call fails
     */
    public Optional<String> generate(String systemPrompt, List<ChatTurn> turns, int maxOutputTokens) {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            return Optional.empty();
        }
        try {
            List<Map<String, Object>> contents = turns.stream()
                    .map(turn -> Map.of("role", turn.role(), "parts", List.of(Map.of("text", turn.text()))))
                    .toList();

            Map<String, Object> body = Map.of(
                    "contents", contents,
                    "systemInstruction", Map.of("parts", List.of(Map.of("text", systemPrompt))),
                    "generationConfig", Map.of("temperature", 0.5, "maxOutputTokens", maxOutputTokens));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", properties.apiKey());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            String url = GENERATE_URL.formatted(properties.model());
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            return extractText(response);
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private Optional<String> extractText(Map<String, Object> response) {
        if (response == null) {
            return Optional.empty();
        }
        Object candidatesRaw = response.get("candidates");
        if (!(candidatesRaw instanceof List<?> candidates) || candidates.isEmpty()) {
            return Optional.empty();
        }
        Object first = candidates.get(0);
        if (!(first instanceof Map<?, ?> candidate)) {
            return Optional.empty();
        }
        Object content = candidate.get("content");
        if (!(content instanceof Map<?, ?> contentMap)) {
            return Optional.empty();
        }
        Object partsRaw = contentMap.get("parts");
        if (!(partsRaw instanceof List<?> parts) || parts.isEmpty()) {
            return Optional.empty();
        }
        Object firstPart = parts.get(0);
        if (!(firstPart instanceof Map<?, ?> part)) {
            return Optional.empty();
        }
        Object text = part.get("text");
        return text instanceof String s && !s.isBlank() ? Optional.of(s) : Optional.empty();
    }

    public record ChatTurn(String role, String text) {
    }
}