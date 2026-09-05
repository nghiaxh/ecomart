package com.ecomart.service;

import com.ecomart.domain.enums.OrderStatus;
import com.ecomart.domain.enums.PaymentStatus;
import com.ecomart.domain.enums.UserRole;
import com.ecomart.dto.response.AdminDashboardResponse;
import com.ecomart.repository.OrderRepository;
import com.ecomart.repository.ProductRepository;
import com.ecomart.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
