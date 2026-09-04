package com.ecomart.security;

import com.ecomart.config.JwtProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    private static final String VALID_SECRET = "0123456789abcdef0123456789abcdef"; // 32 bytes

    private JwtTokenProvider provider(String secret) {
        return new JwtTokenProvider(new JwtProperties(secret, 3600000L));
    }

    @Test
    void rejectsBlankSecret() {
        assertThrows(IllegalStateException.class, () -> provider(""));
        assertThrows(IllegalStateException.class, () -> provider(null));
    }

    @Test
    void rejectsShortSecret() {
        assertThrows(IllegalStateException.class, () -> provider("short"));
    }

    @Test
    void acceptsValidSecret() {
        assertDoesNotThrow(() -> provider(VALID_SECRET));
    }

    @Test
    void generateAndValidateRoundTrip() {
        JwtTokenProvider p = provider(VALID_SECRET);
        String token = p.generateToken(42L, "CUSTOMER");
        assertNotNull(token);
        assertTrue(p.validate(token));
        assertEquals(42L, p.getUserId(token));
        assertEquals("CUSTOMER", p.getClaims(token).get("role"));
    }

    @Test
    void rejectsGarbageToken() {
        JwtTokenProvider p = provider(VALID_SECRET);
        assertFalse(p.validate("not-a-jwt"));
    }

    @Test
    void rejectsTokenSignedWithDifferentKey() {
        JwtTokenProvider p1 = provider(VALID_SECRET);
        JwtTokenProvider p2 = provider("fedcba9876543210fedcba9876543210");
        String token = p1.generateToken(1L, "ADMIN");
        assertFalse(p2.validate(token));
    }
}