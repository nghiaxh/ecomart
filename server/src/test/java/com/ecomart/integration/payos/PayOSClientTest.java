package com.ecomart.integration.payos;

import com.ecomart.config.PayOSProperties;
import com.ecomart.common.HmacTestUtil;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class PayOSClientTest {

    private PayOSClient client(String checksumKey) {
        PayOSProperties props = new PayOSProperties("client", "api", checksumKey, "http://r", "http://c");
        return new PayOSClient(props, mock(RestTemplate.class));
    }

    @Test
    void degradesGracefullyWhenChecksumKeyMissing() {
        PayOSClient c = client(null);
        assertTrue(c.verifySignature(Map.of("amount", 10000), "anything"));
    }

    @Test
    void rejectsMissingDataOrSignature() {
        PayOSClient c = client("secret-key");
        assertFalse(c.verifySignature(null, "abc"));
        assertFalse(c.verifySignature(Map.of("amount", 1), null));
        assertFalse(c.verifySignature(Map.of("amount", 1), "  "));
    }

    @Test
    void acceptsCorrectSignature() {
        PayOSClient c = client("secret-key");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("orderCode", 100L);
        data.put("amount", 25000L);
        String sig = HmacTestUtil.hmac("secret-key", "amount=25000&orderCode=100");
        assertTrue(c.verifySignature(data, sig));
    }

    @Test
    void rejectsTamperedData() {
        PayOSClient c = client("secret-key");
        Map<String, Object> data = Map.of("orderCode", 100L, "amount", 25000L);
        String sig = HmacTestUtil.hmac("secret-key", "amount=25000&orderCode=100");
        assertFalse(c.verifySignature(Map.of("orderCode", 999L, "amount", 25000L), sig));
    }
}