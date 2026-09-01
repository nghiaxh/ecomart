package com.ecomart.integration.gemini;

import com.ecomart.config.GeminiProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class GeminiClient {

    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";

    private final GeminiProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeminiClient(GeminiProperties properties, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.properties = properties;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Simple non-RAG chat. Returns the model text, or a fallback message when
     * the Gemini API key is not configured.
     */
    public String chat(String userMessage) {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            return "Cảm ơn bạn đã hỏi về EcoMart! Hiện tại trợ lý AI chưa được kích hoạt. "
                    + "Bạn có thể khám phá các sản phẩm trong siêu thị của chúng tôi.";
        }
        try {
            Map<String, Object> content = Map.of(
                    "contents", List.of(Map.of(
                            "role", "user",
                            "parts", List.of(Map.of("text",
                                    "Bạn là trợ lý của EcoMart, một siêu thị trực tuyến Việt Nam. "
                                            + "Trả lời ngắn gọn, thân thiện bằng tiếng Việt về sản phẩm, khuyến mãi và đặt hàng. "
                                            + "Câu hỏi: " + userMessage)))));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String url = String.format(API_URL, properties.model()) + "?key=" + properties.apiKey();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(content, headers);
            String raw = restTemplate.postForObject(url, entity, String.class);
            return extractText(raw);
        } catch (Exception ex) {
            return "Xin lỗi, tôi không thể trả lời ngay lúc này. Bạn vui lòng thử lại sau nhé!";
        }
    }

    private String extractText(String raw) {
        try {
            JsonNode root = objectMapper.readTree(raw);
            JsonNode text = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
            return text.isMissingNode() ? "Không có phản hồi." : text.asText();
        } catch (Exception ex) {
            return "Không có phản hồi.";
        }
    }
}
