package com.ecomart.service;

import com.ecomart.common.SecurityUtils;
import com.ecomart.config.ShopProperties;
import com.ecomart.domain.entity.Address;
import com.ecomart.domain.entity.Cart;
import com.ecomart.domain.entity.CartItem;
import com.ecomart.domain.entity.Customer;
import com.ecomart.domain.entity.Order;
import com.ecomart.domain.entity.Payment;
import com.ecomart.domain.entity.Product;
import com.ecomart.domain.enums.NotificationType;
import com.ecomart.domain.enums.OrderStatus;
import com.ecomart.domain.enums.PaymentMethod;
import com.ecomart.domain.enums.PaymentStatus;
import com.ecomart.dto.request.CheckoutRequest;
import com.ecomart.dto.response.CheckoutResponse;
import com.ecomart.exception.BadRequestException;
import com.ecomart.integration.payos.PayOSClient;
import com.ecomart.repository.AddressRepository;
import com.ecomart.repository.CartItemRepository;
import com.ecomart.repository.OrderItemRepository;
import com.ecomart.repository.OrderRepository;
import com.ecomart.repository.PaymentRepository;
import com.ecomart.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock SecurityUtils securityUtils;
    @Mock CartService cartService;
    @Mock CartItemRepository cartItemRepository;
    @Mock AddressRepository addressRepository;
    @Mock OrderRepository orderRepository;
    @Mock OrderItemRepository orderItemRepository;
    @Mock PaymentRepository paymentRepository;
    @Mock ProductRepository productRepository;
    @Mock NotificationService notificationService;
    @Mock PayOSClient payOSClient;

    private OrderService service;

    @BeforeEach
    void setUp() {
        service = new OrderService(securityUtils, cartService, cartItemRepository, addressRepository,
                orderRepository, orderItemRepository, paymentRepository, productRepository,
                notificationService, payOSClient, new ShopProperties(20000), mock(ActivityLogService.class));
    }

    private Customer customer(long id) {
        Customer c = new Customer();
        c.setId(id);
        return c;
    }

    private Product product(long id, int stock, boolean active) {
        Product p = new Product();
        p.setId(id);
        p.setName("Bơ");
        p.setPrice(25000);
        p.setStock(stock);
        p.setActive(active);
        return p;
    }

    private Address address() {
        Address a = new Address();
        a.setId(1L);
        a.setReceiverName("Nguyen Van A");
        a.setReceiverPhone("0901234567");
        a.setStreet("12 Nguyen Hue");
        a.setWard("Ben Nghe");
        a.setDistrict("Q1");
        a.setCity("HCM");
        return a;
    }

    private Cart cartWith(Product product, int quantity) {
        Cart cart = new Cart();
        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(quantity);
        cart.setItems(new ArrayList<>(java.util.List.of(item)));
        return cart;
    }

    @Test
    void checkoutRejectsEmptyCart() {
        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());
        when(cartService.getCart()).thenReturn(cart);
        assertThrows(BadRequestException.class,
                () -> service.checkout(new CheckoutRequest(1L, PaymentMethod.COD, null)));
    }

    @Test
    void checkoutRejectsInactiveProduct() {
        Cart cart = cartWith(product(1L, 10, false), 2);
        when(cartService.getCart()).thenReturn(cart);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address()));
        when(securityUtils.currentUser()).thenReturn(customer(7L));

        assertThrows(BadRequestException.class,
                () -> service.checkout(new CheckoutRequest(1L, PaymentMethod.COD, null)));
    }

    @Test
    void checkoutRejectsInsufficientStock() {
        Cart cart = cartWith(product(1L, 2, true), 5);
        when(cartService.getCart()).thenReturn(cart);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address()));
        when(securityUtils.currentUser()).thenReturn(customer(7L));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.checkout(new CheckoutRequest(1L, PaymentMethod.COD, null)));

        assertEquals("Product Bơ has insufficient stock", ex.getMessage());
    }

    @Test
    void checkoutWithCodBuildsOrderWithShippingFeeAndDecrementsStock() {
        Product product = product(1L, 10, true);
        Cart cart = cartWith(product, 2);
        Customer customer = customer(7L);
        when(cartService.getCart()).thenReturn(cart);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address()));
        when(securityUtils.currentUser()).thenReturn(customer);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        CheckoutResponse response = service.checkout(new CheckoutRequest(1L, PaymentMethod.COD, "giao gio hanh chinh"));

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, org.mockito.Mockito.times(3)).save(orderCaptor.capture());
        Order order = orderCaptor.getValue();
        assertEquals(50000.0, order.getSubtotal());
        assertEquals(20000.0, order.getShippingFee());
        assertEquals(70000.0, order.getTotal());
        assertEquals(OrderStatus.PENDING, order.getStatus());

        assertEquals("Order placed successfully, pay on delivery", response.message());
        assertNull(response.payosCheckoutUrl());
        assertEquals("PENDING", response.status());

        verify(productRepository).save(product);
        assertEquals(8, product.getStock());
        assertEquals(0, cart.getItems().size());
        verify(cartItemRepository).deleteByCartId(any());
        verify(notificationService).send(eq(customer), contains("Order #"), anyString(),
                eq(NotificationType.ORDER), anyString());
    }

    @Test
    void confirmPaymentDoesNotChangeCodPayment() {
        Payment payment = new Payment();
        payment.setMethod(PaymentMethod.COD);
        payment.setStatus(PaymentStatus.PENDING);
        Order order = new Order();
        order.setId(2L);
        order.setCustomer(customer(1L));
        order.setStatus(OrderStatus.PENDING);
        order.setPayment(payment);
        when(orderRepository.findById(2L)).thenReturn(Optional.of(order));

        service.confirmPayment(2L);

        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void confirmPaymentMarksPayosPaymentPaidAndNotifies() {
        Payment payment = new Payment();
        payment.setMethod(PaymentMethod.PAYOS);
        payment.setStatus(PaymentStatus.PENDING);
        Customer customer = customer(1L);
        Order order = new Order();
        order.setId(2L);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);
        order.setPayment(payment);
        when(orderRepository.findById(2L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        service.confirmPayment(2L);

        assertEquals(PaymentStatus.PAID, payment.getStatus());
        verify(paymentRepository).save(payment);
        verify(notificationService).send(eq(customer), contains("Payment"), anyString(),
                eq(NotificationType.ORDER), anyString());
    }

    @Test
    void allOrdersReturnsAllWhenStatusNull() {
        org.springframework.data.domain.Page<Order> page =
                new org.springframework.data.domain.PageImpl<>(java.util.List.of());
        when(orderRepository.findAll(org.springframework.data.domain.PageRequest.of(0, 10))).thenReturn(page);
        service.allOrders(null, org.springframework.data.domain.PageRequest.of(0, 10));
    }

    @Test
    void updateStatusAppliesNewStatus() {
        Order order = new Order();
        order.setId(5L);
        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));
        service.updateStatus(5L, OrderStatus.CONFIRMED);
    }

    @Test
    void confirmPaymentByCurrentUserRejectsForeignOrderForNonAdmin() {
        Customer owner = customer(1L);
        Order order = new Order();
        order.setId(2L);
        order.setCustomer(owner);

        when(securityUtils.currentUser()).thenReturn(customer(99L));
        when(securityUtils.currentUserHasRole("ADMIN")).thenReturn(false);
        when(orderRepository.findById(2L)).thenReturn(Optional.of(order));

        assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> service.confirmPaymentByCurrentUser(2L));
    }

    @Test
    void confirmPaymentByCurrentUserAllowsAdmin() {
        Customer owner = customer(1L);
        Payment payment = new Payment();
        payment.setMethod(PaymentMethod.PAYOS);
        payment.setStatus(PaymentStatus.PENDING);
        Order order = new Order();
        order.setId(2L);
        order.setCustomer(owner);
        order.setStatus(OrderStatus.PENDING);
        order.setPayment(payment);

        when(securityUtils.currentUser()).thenReturn(customer(99L));
        when(securityUtils.currentUserHasRole("ADMIN")).thenReturn(true);
        when(orderRepository.findById(2L)).thenReturn(Optional.of(order));

        service.confirmPaymentByCurrentUser(2L);
    }
}