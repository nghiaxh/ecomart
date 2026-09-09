package com.ecomart.dto.response;

import com.ecomart.domain.enums.OrderStatus;

import java.util.List;
import java.util.Map;

public record AdminStatisticsResponse(
        List<DailyStat> revenueSeries,
        Map<OrderStatus, Long> ordersByStatus,
        List<TopProduct> topProducts
) {
    public record DailyStat(
            String date,
            double revenue,
            long orderCount
    ) {
    }

    public record TopProduct(
            Long productId,
            String name,
            long quantity,
            double revenue
    ) {
    }
}
