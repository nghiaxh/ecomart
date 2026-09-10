package com.ecomart.repository;

import com.ecomart.AbstractPostgresIntegrationTest;
import com.ecomart.domain.entity.Category;
import com.ecomart.domain.entity.Customer;
import com.ecomart.domain.entity.Order;
import com.ecomart.domain.entity.OrderItem;
import com.ecomart.domain.entity.OrderItemId;
import com.ecomart.domain.entity.Product;
import com.ecomart.domain.entity.User;
import com.ecomart.domain.enums.OrderStatus;
import com.ecomart.domain.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CompositeKeyAndInheritanceTest extends AbstractPostgresIntegrationTest {

    @Autowired UserRepository userRepository;
    @Autowired CustomerRepository customerRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired OrderItemRepository orderItemRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired ProductRepository productRepository;

    @Test
    void joinedInheritancePersistsCustomerAsUser() {
        Customer c = new Customer();
        c.setUsername("minh");
        c.setEmail("minh@ecomart.test");
        c.setNumberPhone("0901234567");
        c.setPasswordHash("encoded");
        c.setRole(UserRole.CUSTOMER);
        Long id = customerRepository.save(c).getId();

        Optional<User> reloaded = userRepository.findById(id);

        assertTrue(reloaded.isPresent());
        assertTrue(reloaded.get() instanceof Customer);
        assertEquals("minh@ecomart.test", reloaded.get().getEmail());
        assertEquals(UserRole.CUSTOMER, reloaded.get().getRole());
    }

    @Test
    void compositeEmbeddedIdRoundTrips() {
        Customer c = new Customer();
        c.setUsername("khach");
        c.setEmail("khach@ecomart.test");
        c.setNumberPhone("0901234567");
        c.setPasswordHash("encoded");
        c.setRole(UserRole.CUSTOMER);
        customerRepository.save(c);

        Category category = new Category();
        category.setName("Trai cay");
        category.setSlug("trai-cay");
        categoryRepository.save(category);

        Product product = new Product();
        product.setCategory(category);
        product.setName("Bo");
        product.setSlug("bo");
        product.setPrice(25000);
        product.setStock(10);
        product.setActive(true);
        productRepository.save(product);

        Order order = new Order();
        order.setCustomer(c);
        order.setReceiverName("Nguyen Van A");
        order.setReceiverPhone("0901234567");
        order.setAddress("12 Nguyen Hue, Q1, HCM");
        order.setStatus(OrderStatus.PENDING);
        order.setTotal(75000);
        orderRepository.save(order);

        OrderItem item = new OrderItem();
        item.setId(new OrderItemId(order.getId(), product.getId()));
        item.setOrder(order);
        item.setProduct(product);
        item.setProductNameSnapshot(product.getName());
        item.setQuantity(3);
        item.setUnitPrice(25000);
        orderItemRepository.save(item);

        Optional<OrderItem> loaded = orderItemRepository.findById(new OrderItemId(order.getId(), product.getId()));

        assertTrue(loaded.isPresent());
        assertEquals(3, loaded.get().getQuantity());
        assertEquals(product.getName(), loaded.get().getProductNameSnapshot());
    }
}