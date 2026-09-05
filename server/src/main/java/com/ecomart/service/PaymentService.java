package com.ecomart.service;

import com.ecomart.dto.response.MessageResponse;
import com.ecomart.dto.response.OrderResponse;
import com.ecomart.exception.BadRequestException;
import com.ecomart.exception.UnauthorizedException;
import com.ecomart.integration.payos.PayOSClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class PaymentService {

    private final OrderService orderService;
    private final PayOSClient payOSClient;

    public PaymentService(OrderService orderService, PayOSClient payOSClient) {
        this.orderService = orderService;
        this.payOSClient = payOSClient;
    }

    @Transactional
    public OrderResponse handleReturn(Long orderId) {
        return orderService.confirmPaymentByCurrentUser(orderId);
    }

    @Transactional
    public MessageResponse handleWebhook(Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        Map<String, Object> data = body == null ? null : (Map<String, Object>) body.get("data");
        String signature = body == null ? null : (String) body.get("signature");

        if (data == null || data.get("orderCode") == null) {
            throw new BadRequestException("Dữ liệu webhook không hợp lệ");
        }
        if (!payOSClient.verifySignature(data, signature)) {
            throw new UnauthorizedException("Chữ ký PayOS không hợp lệ");
        }
        long orderId;
        try {
            orderId = Long.parseLong(String.valueOf(data.get("orderCode")));
        } catch (NumberFormatException ex) {
            throw new BadRequestException("Mã đơn hàng không hợp lệ");
        }
        orderService.confirmPayment(orderId);
        return new MessageResponse("Thanh toán đã được xác nhận");
    }
}
