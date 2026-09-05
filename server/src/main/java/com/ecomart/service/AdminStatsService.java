package com.ecomart.service;

import com.ecomart.domain.entity.Customer;
import com.ecomart.domain.enums.OrderStatus;
import com.ecomart.domain.enums.PaymentStatus;
import com.ecomart.domain.enums.UserRole;
import com.ecomart.dto.response.AdminDashboardResponse;
import com.ecomart.repository.OrderRepository;
import com.ecomart.repository.ProductRepository;
import com.ecomart.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
