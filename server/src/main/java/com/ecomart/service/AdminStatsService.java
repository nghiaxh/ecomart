package com.ecomart.service;

import com.ecomart.domain.entity.Customer;
import com.ecomart.domain.enums.OrderStatus;
import com.ecomart.domain.enums.PaymentStatus;
import com.ecomart.domain.enums.UserRole;
import com.ecomart.repository.OrderRepository;
import com.ecomart.repository.ProductRepository;
import com.ecomart.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
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
    public Map<String, Object> dashboard() {
        long productCount = productRepository.count();
        long customerCount = userRepository.countByRole(UserRole.CUSTOMER);
        long orderCount = orderRepository.count();
        Long revenue = orderRepository.sumTotalByStatusAndPaid(OrderStatus.COMPLETED, PaymentStatus.PAID);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("productCount", productCount);
        result.put("customerCount", customerCount);
        result.put("orderCount", orderCount);
        result.put("revenue", revenue == null ? 0.0 : revenue.doubleValue());
        return result;
    }
}
