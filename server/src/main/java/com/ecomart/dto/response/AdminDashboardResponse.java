package com.ecomart.dto.response;

public record AdminDashboardResponse(
        long productCount,
        long customerCount,
        long orderCount,
        double revenue
) {
}
