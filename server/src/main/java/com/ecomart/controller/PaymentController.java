package com.ecomart.controller;

import com.ecomart.dto.response.MessageResponse;
import com.ecomart.dto.response.OrderResponse;
import com.ecomart.service.PaymentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * PayOS success return navigated by the customer's browser. Confirms the
     * payment only when the requested order belongs to the authenticated user
     * (or the user is an admin).
     */
    @PostMapping("/payos/return")
    @PreAuthorize("isAuthenticated()")
    public OrderResponse payosReturn(@RequestParam Long orderId) {
        return paymentService.handleReturn(orderId);
    }

    /**
     * PayOS webhook. On a real PayOS integration this is invoked by PayOS with
     * a signed payload. The HMAC signature is verified against the checksum key
     * before marking the payment as paid, so it must NOT require cookie/JWT auth.
     */
    @PostMapping("/payos/webhook")
    public MessageResponse payosWebhook(@RequestBody Map<String, Object> body) {
        return paymentService.handleWebhook(body);
    }
}
