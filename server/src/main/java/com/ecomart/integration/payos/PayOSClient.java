package com.ecomart.integration.payos;

import com.ecomart.config.PayOSProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class PayOSClient {

    private static final String CREATE_PAYMENT_URL = "https://api-merchant.payos.vn/v2/payment-requests";
    private static final String HMAC_ALGORITHM = "HmacSHA256";

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

    /**
     * Verifies the HMAC-SHA256 signature of a PayOS webhook/return payload.
     * The signature is computed over the alphabetical concatenation of
     * "key=value" pairs of every field in the {@code data} object.
     *
     * @return true when the payload signature matches, or when PayOS keys are
     *         not configured (so the flow degrades gracefully in dev). False
     *         otherwise.
     */
    public boolean verifySignature(Map<String, Object> data, String signature) {
        if (isBlank(properties.checksumKey())) {
            return true;
        }
        if (data == null || signature == null || signature.isBlank()) {
            return false;
        }
        try {
            String canonical = data.entrySet().stream()
                    .filter(e -> e.getValue() != null)
                    .sorted(Map.Entry.comparingByKey())
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .reduce((a, b) -> a + "&" + b)
                    .orElse("");
            String expected = hmac(properties.checksumKey(), canonical);
            return expected.equalsIgnoreCase(signature);
        } catch (Exception ex) {
            return false;
        }
    }

    private String hmac(String key, String data) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        SecretKeySpec spec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
        mac.init(spec);
        return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
