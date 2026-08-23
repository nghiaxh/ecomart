package com.ecomart.controller;

import com.ecomart.dto.response.MessageResponse;
import com.ecomart.dto.response.OrderResponse;
import com.ecomart.service.OrderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final OrderService orderService;

    public PaymentController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * PayOS success return. Marks the order payment as paid.
     */
    @PostMapping("/payos/return")
    public OrderResponse payosReturn(@RequestParam Long orderId) {
        return orderService.confirmPayment(orderId);
    }
}
