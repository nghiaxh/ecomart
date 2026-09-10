package com.ecomart.repository;

import com.ecomart.AbstractPostgresIntegrationTest;
import com.ecomart.domain.entity.Category;
import com.ecomart.domain.entity.Customer;
import com.ecomart.domain.entity.Order;
import com.ecomart.domain.entity.OrderItem;
import com.ecomart.domain.entity.OrderItemId;
import com.ecomart.domain.entity.Payment;
import com.ecomart.domain.entity.Product;
import com.ecomart.domain.enums.OrderStatus;
import com.ecomart.domain.enums.PaymentMethod;
import com.ecomart.domain.enums.PaymentStatus;
import com.ecomart.domain.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderRepositoryTest extends AbstractPostgresIntegrationTest {

    @Autowired OrderRepository orderRepository;
    @Autowired OrderItemRepository orderItemRepository;
    @Autowired CustomerRepository customerRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired ProductRepository productRepository;
    @Autowired PaymentRepository paymentRepository;

    private Customer customer(String email) {
        Customer c = new Customer();
        c.setUsername("minh" + email.replaceAll("[^a-z]", ""));
        c.setEmail(email);
        c.setNumberPhone("0901234567");
        c.setPasswordHash("encoded");
        c.setRole(UserRole.CUSTOMER);
        return customerRepository.save(c);
    }

    private Category category() {
        long unique = System.nanoTime();
        Category c = new Category();
        c.setName("Trai cay " + unique);
        c.setSlug("trai-cay-" + unique);
        return categoryRepository.save(c);
    }

    private Product product(String slug) {
        Product p = new Product();
        p.setCategory(category());
        p.setName("San pham " + slug);
        p.setSlug(slug + "-" + System.nanoTime());
        p.setPrice(20000);
        p.setStock(10);
        p.setActive(true);
        return productRepository.save(p);
    }

    private Order order(Customer c, OrderStatus status, double total) {
        Order o = new Order();
        o.setCustomer(c);
        o.setReceiverName("Nguyen Van A");
        o.setReceiverPhone("0901234567");
        o.setAddress("12 Nguyen Hue, Q1, HCM");
        o.setStatus(status);
        o.setTotal(total);
        return orderRepository.save(o);
    }

    private OrderItem addItem(Order order, Product product, int quantity, double unitPrice) {
        OrderItem oi = new OrderItem();
        oi.setId(new OrderItemId(order.getId(), product.getId()));
        oi.setOrder(order);
        oi.setProduct(product);
        oi.setProductNameSnapshot(product.getName());
        oi.setQuantity(quantity);
        oi.setUnitPrice(unitPrice);
        return orderItemRepository.save(oi);
    }

    @Test
    void sumTotalByStatusAndPaidSumsCompletedAndPaidOrders() {
        Customer c = customer("sum@ecomart.test");
        Order completed = order(c, OrderStatus.COMPLETED, 100);
        Payment paidCompleted = new Payment();
        paidCompleted.setOrder(completed);
        paidCompleted.setMethod(PaymentMethod.PAYOS);
        paidCompleted.setStatus(PaymentStatus.PAID);
        paidCompleted.setAmount(100);
        paymentRepository.save(paidCompleted);
        Order paidPending = order(c, OrderStatus.PENDING, 50);
        Payment paid = new Payment();
        paid.setOrder(paidPending);
        paid.setMethod(PaymentMethod.PAYOS);
        paid.setStatus(PaymentStatus.PAID);
        paid.setAmount(50);
        paymentRepository.save(paid);
        order(c, OrderStatus.PENDING, 30);

        Long sum = orderRepository.sumTotalByStatusAndPaid(OrderStatus.COMPLETED, PaymentStatus.PAID);

        assertEquals(150L, sum);
    }

    @Test
    void countByStatusGroupsOrdersPerStatus() {
        Customer c = customer("count@ecomart.test");
        order(c, OrderStatus.PENDING, 10);
        order(c, OrderStatus.PENDING, 20);
        order(c, OrderStatus.COMPLETED, 30);

        List<OrderRepository.StatusCountRow> rows = orderRepository.countByStatus();
        Map<OrderStatus, Long> grouped = rows.stream()
                .collect(Collectors.toMap(OrderRepository.StatusCountRow::getStatus,
                        OrderRepository.StatusCountRow::getCount));

        assertEquals(2L, grouped.get(OrderStatus.PENDING));
        assertEquals(1L, grouped.get(OrderStatus.COMPLETED));
    }

    @Test
    void topProductsOrdersByTotalQuantity() {
        Customer c = customer("top@ecomart.test");
        Product bo = product("bo");
        Product xoai = product("xoai");
        Order order = order(c, OrderStatus.COMPLETED, 160);
        addItem(order, bo, 5, 20000);
        addItem(order, xoai, 3, 20000);

        List<OrderRepository.TopProductRow> rows = orderRepository.topProducts(PageRequest.of(0, 10));

        assertNotNull(rows);
        assertEquals(bo.getId(), rows.get(0).getProductId());
        assertEquals(5L, rows.get(0).getQuantity());
        assertEquals(100000.0, rows.get(0).getRevenue(), 0.001);
        assertEquals(xoai.getId(), rows.get(1).getProductId());
        assertEquals(60000.0, rows.get(1).getRevenue(), 0.001);
    }
}