package com.ecomart.service;

import com.ecomart.common.Mapper;
import com.ecomart.common.SecurityUtils;
import com.ecomart.domain.entity.*;
import com.ecomart.domain.enums.*;
import com.ecomart.dto.request.CheckoutRequest;
import com.ecomart.dto.response.CheckoutResponse;
import com.ecomart.dto.response.OrderResponse;
import com.ecomart.dto.response.PageResponse;
import com.ecomart.exception.BadRequestException;
import com.ecomart.exception.ResourceNotFoundException;
import com.ecomart.integration.payos.PayOSClient;
import com.ecomart.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final SecurityUtils securityUtils;
    private final CartService cartService;
    private final CartItemRepository cartItemRepository;
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final NotificationService notificationService;
    private final PayOSClient payOSClient;

    private static final double SHIPPING_FEE = 20000;

    public OrderService(SecurityUtils securityUtils,
                        CartService cartService,
                        CartItemRepository cartItemRepository,
                        AddressRepository addressRepository,
                        OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        PaymentRepository paymentRepository,
                        ProductRepository productRepository,
                        CustomerRepository customerRepository,
                        NotificationService notificationService,
                        PayOSClient payOSClient) {
        this.securityUtils = securityUtils;
        this.cartService = cartService;
        this.cartItemRepository = cartItemRepository;
        this.addressRepository = addressRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentRepository = paymentRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.notificationService = notificationService;
        this.payOSClient = payOSClient;
    }

    @Transactional
    public CheckoutResponse checkout(CheckoutRequest request) {
        Cart cart = cartService.getCart();
        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Giỏ hàng trống");
        }
        Address address = addressRepository.findById(request.addressId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ giao hàng"));
        Customer customer = (Customer) securityUtils.currentUser();

        PaymentMethod method = parseMethod(request.paymentMethod());

        for (CartItem ci : cart.getItems()) {
            Product p = ci.getProduct();
            if (!p.isActive()) {
                throw new BadRequestException("Sản phẩm " + p.getName() + " đã ngừng kinh doanh");
            }
            if (ci.getQuantity() > p.getStock()) {
                throw new BadRequestException("Sản phẩm " + p.getName() + " không đủ hàng");
            }
        }

        double subtotal = 0;
        for (CartItem ci : cart.getItems()) {
            Product p = ci.getProduct();
            subtotal += p.getPrice() * ci.getQuantity();
        }

        Order order = new Order();
        order.setCustomer(customer);
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setAddress(address.getStreet() + ", " + address.getWard() + ", " + address.getDistrict() + ", " + address.getCity());
        order.setStatus(OrderStatus.PENDING);
        order.setSubtotal(subtotal);
        order.setShippingFee(SHIPPING_FEE);
        order.setTotal(subtotal + SHIPPING_FEE);
        order.setNotes(request.notes());
        order = orderRepository.save(order);

        List<OrderItem> items = new ArrayList<>();
        for (CartItem ci : cart.getItems()) {
            Product p = ci.getProduct();
            OrderItem oi = new OrderItem();
            oi.setId(new OrderItemId(order.getId(), p.getId()));
            oi.setOrder(order);
            oi.setProduct(p);
            oi.setProductNameSnapshot(p.getName());
            oi.setQuantity(ci.getQuantity());
            oi.setUnitPrice(p.getPrice());
            orderItemRepository.save(oi);
            items.add(oi);

            p.setStock(p.getStock() - ci.getQuantity());
            productRepository.save(p);
        }
        order.setItems(items);

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setMethod(method);
        payment.setStatus(method == PaymentMethod.COD ? PaymentStatus.PENDING : PaymentStatus.PENDING);
        payment.setAmount(order.getTotal());
        paymentRepository.save(payment);
        order.setPayment(payment);
        orderRepository.save(order);

        String checkoutUrl = null;
        if (method == PaymentMethod.PAYOS) {
            checkoutUrl = payOSClient.createCheckoutUrl(order.getId(), Math.round(order.getTotal()), "EcoMart order #" + order.getId());
            if (checkoutUrl != null) {
                payment.setPayosOrderCode(String.valueOf(order.getId()));
                paymentRepository.save(payment);
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);
                throw new BadRequestException("Không thể tạo thanh toán PayOS, vui lòng thử lại hoặc chọn COD");
            }
        }

        cart.getItems().clear();
        cartItemRepository.deleteByCartId(cart.getId());
        notificationService.send(customer, "Đơn hàng #" + order.getId() + " đã được tạo",
                "Đơn hàng của bạn với tổng giá trị " + Math.round(order.getTotal()) + "đ đã được ghi nhận.",
                NotificationType.ORDER, String.valueOf(order.getId()));

        return new CheckoutResponse(order.getId(), order.getStatus().name(), checkoutUrl,
                method == PaymentMethod.COD ? "Đặt hàng thành công, thanh toán khi nhận hàng" : "Vui lòng hoàn tất thanh toán");
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> myOrders(Pageable pageable) {
        Customer customer = (Customer) securityUtils.currentUser();
        Page<Order> page = orderRepository.findByCustomerId(customer.getId(), pageable);
        return Mapper.toPage(page, page.getContent().stream().map(Mapper::toOrder).toList());
    }

    @Transactional(readOnly = true)
    public OrderResponse getMyOrder(Long orderId) {
        Customer customer = (Customer) securityUtils.currentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new BadRequestException("Không thể xem đơn hàng này");
        }
        return Mapper.toOrder(order);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> allOrders(String status, Pageable pageable) {
        Page<Order> page;
        if (status != null && !status.isBlank()) {
            OrderStatus orderStatus;
            try {
                orderStatus = OrderStatus.valueOf(status);
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Trạng thái đơn hàng không hợp lệ: " + status);
            }
            page = orderRepository.findByStatus(orderStatus, pageable);
        } else {
            page = orderRepository.findAll(pageable);
        }
        return Mapper.toPage(page, page.getContent().stream().map(Mapper::toOrder).toList());
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(status);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Trạng thái đơn hàng không hợp lệ: " + status);
        }
        order.setStatus(newStatus);
        orderRepository.save(order);

        return Mapper.toOrder(order);
    }

    @Transactional
    public OrderResponse confirmPayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
        markPaid(order);
        return Mapper.toOrder(order);
    }

    @Transactional
    public OrderResponse confirmPaymentByCurrentUser(Long orderId) {
        Customer customer = (Customer) securityUtils.currentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
        if (!order.getCustomer().getId().equals(customer.getId())
                && !securityUtils.currentUserHasRole("ADMIN")) {
            throw new com.ecomart.exception.UnauthorizedException("Không thể thanh toán đơn hàng này");
        }
        markPaid(order);
        return Mapper.toOrder(order);
    }

    private void markPaid(Order order) {
        Payment payment = order.getPayment();
        if (payment != null && payment.getMethod() == PaymentMethod.PAYOS && payment.getStatus() != PaymentStatus.PAID) {
            payment.setStatus(PaymentStatus.PAID);
            payment.setPaidAt(java.time.Instant.now());
            paymentRepository.save(payment);
            notificationService.send(order.getCustomer(),
                    "Thanh toán đơn hàng #" + order.getId() + " thành công",
                    "Cảm ơn bạn! Thanh toán cho đơn hàng đã được hoàn tất.",
                    NotificationType.ORDER, String.valueOf(order.getId()));
        }
    }

    private PaymentMethod parseMethod(String value) {
        try {
            return PaymentMethod.valueOf(value.toUpperCase());
        } catch (Exception ex) {
            throw new BadRequestException("Phương thức thanh toán không hợp lệ");
        }
    }
}
