package com.ecomart.service;

import com.ecomart.domain.entity.Customer;
import com.ecomart.domain.entity.Order;
import com.ecomart.domain.enums.OrderStatus;
import com.ecomart.domain.enums.PaymentStatus;
import com.ecomart.repository.OrderRepository;
import com.ecomart.repository.ProductRepository;
import com.ecomart.repository.UserRepository;
import org.springframework.stereotype.Service;

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

    public Map<String, Object> dashboard() {
        long productCount = productRepository.count();
        long customerCount = userRepository.count();
        long orderCount = orderRepository.count();
        double revenue = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED
                        || (o.getPayment() != null && o.getPayment().getStatus() == PaymentStatus.PAID))
                .mapToDouble(Order::getTotal)
                .sum();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("productCount", productCount);
        result.put("customerCount", customerCount);
        result.put("orderCount", orderCount);
        result.put("revenue", revenue);
        return result;
    }
}
