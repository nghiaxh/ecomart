package com.ecomart.service;

import com.ecomart.domain.enums.OrderStatus;
import com.ecomart.domain.enums.PaymentStatus;
import com.ecomart.domain.enums.UserRole;
import com.ecomart.dto.response.AdminDashboardResponse;
import com.ecomart.dto.response.AdminStatisticsResponse;
import com.ecomart.repository.OrderRepository;
import com.ecomart.repository.ProductRepository;
import com.ecomart.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminStatsService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public AdminStatsService(UserRepository userRepository,
                             ProductRepository productRepository,
                             OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponse dashboard() {
        long productCount = productRepository.count();
        long customerCount = userRepository.countByRole(UserRole.CUSTOMER);
        long orderCount = orderRepository.count();
        Long revenue = orderRepository.sumTotalByStatusAndPaid(OrderStatus.COMPLETED, PaymentStatus.PAID);
        return new AdminDashboardResponse(productCount, customerCount, orderCount,
                revenue == null ? 0.0 : revenue.doubleValue());
    }

    @Transactional(readOnly = true)
    public AdminStatisticsResponse statistics(int days) {
        int span = Math.max(days, 1);
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        LocalDate since = today.minusDays(span - 1L);
        Instant sinceInstant = since.atStartOfDay(ZoneId.systemDefault()).toInstant();

        Map<String, MutableDaily> daily = new LinkedHashMap<>();
        for (int i = 0; i < span; i++) {
            LocalDate d = since.plusDays(i);
            daily.put(d.toString(), new MutableDaily(0.0, 0L));
        }
        for (OrderRepository.OrderStatRow row : orderRepository.orderStatsSince(sinceInstant)) {
            LocalDate date = row.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate();
            MutableDaily bucket = daily.get(date.toString());
            if (bucket == null) {
                continue;
            }
            bucket.orderCount++;
            boolean done = row.getStatus() == OrderStatus.COMPLETED
                    || row.getPaymentStatus() == PaymentStatus.PAID;
            if (done) {
                bucket.revenue += row.getTotal();
            }
        }
        List<AdminStatisticsResponse.DailyStat> series = daily.entrySet().stream()
                .map(e -> new AdminStatisticsResponse.DailyStat(e.getKey(), e.getValue().revenue, e.getValue().orderCount))
                .toList();

        Map<OrderStatus, Long> byStatus = new EnumMap<>(OrderStatus.class);
        for (OrderStatus s : OrderStatus.values()) {
            byStatus.put(s, 0L);
        }
        for (OrderRepository.StatusCountRow row : orderRepository.countByStatus()) {
            if (row.getStatus() != null) {
                byStatus.put(row.getStatus(), row.getCount());
            }
        }

        List<AdminStatisticsResponse.TopProduct> top = orderRepository.topProducts(PageRequest.of(0, 5)).stream()
                .map(r -> new AdminStatisticsResponse.TopProduct(r.getProductId(), r.getName(),
                        r.getQuantity(), r.getRevenue()))
                .toList();

        return new AdminStatisticsResponse(series, byStatus, top);
    }

    private static final class MutableDaily {
        double revenue;
        long orderCount;

        MutableDaily(double revenue, long orderCount) {
            this.revenue = revenue;
            this.orderCount = orderCount;
        }
    }
}
