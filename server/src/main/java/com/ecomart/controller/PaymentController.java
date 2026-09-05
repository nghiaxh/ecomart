package com.ecomart.controller;

import com.ecomart.dto.response.MessageResponse;
import com.ecomart.dto.response.OrderResponse;
import com.ecomart.exception.BadRequestException;
import com.ecomart.exception.UnauthorizedException;
import com.ecomart.integration.payos.PayOSClient;
import com.ecomart.service.OrderService;
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

    private final OrderService orderService;
    private final PayOSClient payOSClient;

    public PaymentController(OrderService orderService, PayOSClient payOSClient) {
        this.orderService = orderService;
        this.payOSClient = payOSClient;
    }

    /**
     * PayOS success return navigated by the customer's browser. Confirms the
     * payment only when the requested order belongs to the authenticated user
     * (or the user is an admin).
     */
    @PostMapping("/payos/return")
    @PreAuthorize("isAuthenticated()")
    public OrderResponse payosReturn(@RequestParam Long orderId) {
        return orderService.confirmPaymentByCurrentUser(orderId);
    }

    /**
     * PayOS webhook. On a real PayOS integration this is invoked by PayOS with
     * a signed payload. The HMAC signature is verified against the checksum key
     * before marking the payment as paid, so it must NOT require cookie/JWT auth.
     */
    @PostMapping("/payos/webhook")
    public MessageResponse payosWebhook(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) body.get("data");
        String signature = (String) body.get("signature");

        if (!payOSClient.verifySignature(data, signature)) {
            throw new UnauthorizedException("Chữ ký PayOS không hợp lệ");
        }
        if (data == null || data.get("orderCode") == null) {
            throw new BadRequestException("Dữ liệu webhook không hợp lệ");
        }
        Long orderId;
        try {
            orderId = Long.valueOf(String.valueOf(data.get("orderCode")));
        } catch (NumberFormatException ex) {
            throw new BadRequestException("Mã đơn hàng không hợp lệ");
        }
        orderService.confirmPayment(orderId);
        return new MessageResponse("Thanh toán đã được xác nhận");
    }
}
