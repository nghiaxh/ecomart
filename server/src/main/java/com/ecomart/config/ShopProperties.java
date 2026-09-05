package com.ecomart.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.shop")
public record ShopProperties(double shippingFee) {
}
