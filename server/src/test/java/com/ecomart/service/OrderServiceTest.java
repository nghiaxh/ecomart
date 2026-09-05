package com.ecomart.service;

import com.ecomart.common.SecurityUtils;
import com.ecomart.domain.entity.Address;
import com.ecomart.domain.entity.Cart;
import com.ecomart.domain.entity.CartItem;
import com.ecomart.domain.entity.Customer;
import com.ecomart.domain.entity.Order;
import com.ecomart.domain.entity.Payment;
import com.ecomart.domain.entity.Product;
import com.ecomart.domain.enums.OrderStatus;
import com.ecomart.domain.enums.PaymentMethod;
import com.ecomart.domain.enums.PaymentStatus;
import com.ecomart.dto.request.CheckoutRequest;
import com.ecomart.exception.BadRequestException;
import com.ecomart.exception.UnauthorizedException;
import com.ecomart.integration.payos.PayOSClient;
import com.ecomart.repository.AddressRepository;
import com.ecomart.repository.CartItemRepository;
import com.ecomart.repository.CustomerRepository;
import com.ecomart.repository.OrderItemRepository;
import com.ecomart.repository.OrderRepository;
import com.ecomart.repository.PaymentRepository;
import com.ecomart.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
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
    @Mock CustomerRepository customerRepository;
    @Mock NotificationService notificationService;
    @Mock PayOSClient payOSClient;

    private OrderService service;

    @BeforeEach
    void setUp() {
        service = new OrderService(securityUtils, cartService, cartItemRepository, addressRepository,
                orderRepository, orderItemRepository, paymentRepository, productRepository,
                customerRepository, notificationService, payOSClient);
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

    @Test
    void checkoutRejectsEmptyCart() {
        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());
        when(cartService.getCart()).thenReturn(cart);
        assertThrows(BadRequestException.class,
                () -> service.checkout(new CheckoutRequest(1L, "COD", null)));
    }

    @Test
    void checkoutRejectsInactiveProduct() {
        Cart cart = new Cart();
        CartItem item = new CartItem();
        item.setProduct(product(1L, 10, false));
        item.setQuantity(2);
        cart.setItems(new ArrayList<>(java.util.List.of(item)));
        when(cartService.getCart()).thenReturn(cart);

        Address address = new Address();
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        when(securityUtils.currentUser()).thenReturn(customer(7L));

        assertThrows(BadRequestException.class,
                () -> service.checkout(new CheckoutRequest(1L, "COD", null)));
    }

    @Test
    void allOrdersRejectsInvalidStatus() {
        assertThrows(BadRequestException.class, () -> service.allOrders("NOT_A_STATUS", org.springframework.data.domain.PageRequest.of(0, 10)));
    }

    @Test
    void updateStatusRejectsInvalidStatus() {
        Order order = new Order();
        order.setId(5L);
        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));
        assertThrows(BadRequestException.class, () -> service.updateStatus(5L, "BOGUS"));
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

        assertThrows(UnauthorizedException.class, () -> service.confirmPaymentByCurrentUser(2L));
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