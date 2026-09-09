package com.ecomart.service;

import com.ecomart.domain.enums.OrderStatus;
import com.ecomart.domain.enums.PaymentStatus;
import com.ecomart.domain.enums.UserRole;
import com.ecomart.dto.response.AdminDashboardResponse;
import com.ecomart.dto.response.AdminStatisticsResponse;
import com.ecomart.repository.OrderRepository;
import com.ecomart.repository.ProductRepository;
import com.ecomart.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminStatsServiceTest {

    @Mock UserRepository userRepository;
    @Mock ProductRepository productRepository;
    @Mock OrderRepository orderRepository;

    private AdminStatsService service;

    @BeforeEach
    void setUp() {
        service = new AdminStatsService(userRepository, productRepository, orderRepository);
    }

    @Test
    void dashboardAggregatesCountsAndRevenue() {
        when(productRepository.count()).thenReturn(30L);
        when(userRepository.countByRole(UserRole.CUSTOMER)).thenReturn(12L);
        when(orderRepository.count()).thenReturn(8L);
        when(orderRepository.sumTotalByStatusAndPaid(OrderStatus.COMPLETED, PaymentStatus.PAID)).thenReturn(500000L);

        AdminDashboardResponse result = service.dashboard();

        assertEquals(30L, result.productCount());
        assertEquals(12L, result.customerCount());
        assertEquals(8L, result.orderCount());
        assertEquals(500000.0, result.revenue());
        verify(orderRepository).sumTotalByStatusAndPaid(OrderStatus.COMPLETED, PaymentStatus.PAID);
    }

    @Test
    void dashboardDefaultsRevenueToZeroWhenNull() {
        when(productRepository.count()).thenReturn(0L);
        when(userRepository.countByRole(UserRole.CUSTOMER)).thenReturn(0L);
        when(orderRepository.count()).thenReturn(0L);
        when(orderRepository.sumTotalByStatusAndPaid(OrderStatus.COMPLETED, PaymentStatus.PAID)).thenReturn(null);

        AdminDashboardResponse result = service.dashboard();

        assertEquals(0.0, result.revenue());
    }

    @Test
    void statisticsFillsContiguousSeriesAndStatuses() {
        LocalDate today = LocalDate.now();
        Instant yesterday = today.minusDays(1).atStartOfDay(ZoneId.systemDefault()).plusHours(10).toInstant();
        OrderRepository.OrderStatRow row = mock(OrderRepository.OrderStatRow.class);
        when(row.getCreatedAt()).thenReturn(yesterday);
        when(row.getTotal()).thenReturn(120000.0);
        when(row.getStatus()).thenReturn(OrderStatus.COMPLETED);
        when(orderRepository.orderStatsSince(any())).thenReturn(List.of(row));

        OrderRepository.StatusCountRow statusRow = mock(OrderRepository.StatusCountRow.class);
        when(statusRow.getStatus()).thenReturn(OrderStatus.COMPLETED);
        when(statusRow.getCount()).thenReturn(5L);
        when(orderRepository.countByStatus()).thenReturn(List.of(statusRow));

        when(orderRepository.topProducts(any(Pageable.class))).thenReturn(List.of(
                new OrderRepository.TopProductRow() {
                    @Override
                    public Long getProductId() {
                        return 1L;
                    }

                    @Override
                    public String getName() {
                        return "Bơ";
                    }

                    @Override
                    public long getQuantity() {
                        return 9;
                    }

                    @Override
                    public double getRevenue() {
                        return 225000.0;
                    }
                }
        ));

        AdminStatisticsResponse result = service.statistics(7);

        assertEquals(7, result.revenueSeries().size());
        assertEquals(0.0, result.revenueSeries().get(0).revenue());
        assertTrue(result.revenueSeries().stream()
                .anyMatch(s -> s.revenue() == 120000.0 && s.orderCount() == 1L));
        for (OrderStatus s : OrderStatus.values()) {
            assertEquals(s == OrderStatus.COMPLETED ? 5L : 0L, result.ordersByStatus().get(s), "status " + s);
        }
        assertEquals(1, result.topProducts().size());
        assertEquals("Bơ", result.topProducts().get(0).name());
    }
}
