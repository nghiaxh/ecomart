package com.ecomart.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payos")
public record PayOSProperties(
        String clientId,
        String apiKey,
        String checksumKey,
        String returnUrl,
        String cancelUrl
) {
}
