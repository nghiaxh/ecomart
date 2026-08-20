package com.ecomart.integration.payos;

import com.ecomart.config.PayOSProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class PayOSClient {

    private static final String CREATE_PAYMENT_URL = "https://api-merchant.payos.vn/v2/payment-requests";

    private final PayOSProperties properties;
    private final RestTemplate restTemplate;

    public PayOSClient(PayOSProperties properties, RestTemplate restTemplate) {
        this.properties = properties;
        this.restTemplate = restTemplate;
    }

    /**
     * Simplified PayOS checkout. Returns the checkout URL, or null when the
     * PayOS keys are not configured (falls back to COD).
     */
    public String createCheckoutUrl(Long orderId, long amountInVnd, String description) {
        if (isBlank(properties.clientId()) || isBlank(properties.apiKey())) {
            return null;
        }
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("orderCode", orderId);
            body.put("amount", amountInVnd);
            body.put("description", description);
            body.put("returnUrl", properties.returnUrl());
            body.put("cancelUrl", properties.cancelUrl());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-client-id", properties.clientId());
            headers.set("x-api-key", properties.apiKey());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(CREATE_PAYMENT_URL, entity, Map.class);
            if (response == null) {
                return null;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.get("data");
            return data == null ? null : (String) data.get("checkoutUrl");
        } catch (Exception ex) {
            return null;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
