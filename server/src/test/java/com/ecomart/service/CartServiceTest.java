package com.ecomart.service;

import com.ecomart.common.SecurityUtils;
import com.ecomart.domain.entity.Cart;
import com.ecomart.domain.entity.CartItem;
import com.ecomart.domain.entity.CartItemId;
import com.ecomart.domain.entity.Customer;
import com.ecomart.domain.entity.Product;
import com.ecomart.dto.request.AddToCartRequest;
import com.ecomart.dto.response.CartResponse;
import com.ecomart.exception.BadRequestException;
import com.ecomart.exception.ResourceNotFoundException;
import com.ecomart.repository.CartRepository;
import com.ecomart.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock SecurityUtils securityUtils;
    @Mock CartRepository cartRepository;
    @Mock ProductRepository productRepository;

    private CartService service;

    @BeforeEach
    void setUp() {
        service = new CartService(securityUtils, cartRepository, productRepository);
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
        p.setSlug("bo");
        p.setPrice(25000);
        p.setStock(stock);
        p.setActive(active);
        return p;
    }

    private Cart emptyCart(long id) {
        Cart cart = new Cart();
        cart.setId(id);
        cart.setItems(new ArrayList<>());
        return cart;
    }

    private void addItem(Cart cart, long productId, int quantity) {
        CartItem ci = new CartItem();
        ci.setId(new CartItemId(cart.getId(), productId));
        ci.setCart(cart);
        ci.setProduct(product(productId, 10, true));
        ci.setQuantity(quantity);
        cart.getItems().add(ci);
    }

    @Test
    void addNewProductAddsToCollectionAndReturnsIt() {
        Customer customer = customer(1L);
        Cart cart = emptyCart(1L);
        when(securityUtils.currentUser()).thenReturn(customer);
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product(10L, 10, true)));

        CartResponse response = service.add(new AddToCartRequest(10L, 2));

        assertEquals(1, cart.getItems().size());
        assertEquals(2, cart.getItems().get(0).getQuantity());
        assertEquals(2, response.itemCount());
        assertEquals(10L, response.items().get(0).productId());
        assertEquals(50000, response.subtotal(), 1e-9);
    }

    @Test
    void addExistingProductIncrementsQuantityOnCollectionInstance() {
        Customer customer = customer(1L);
        Cart cart = emptyCart(1L);
        addItem(cart, 10L, 1);
        when(securityUtils.currentUser()).thenReturn(customer);
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product(10L, 10, true)));

        CartResponse response = service.add(new AddToCartRequest(10L, 2));

        assertEquals(1, cart.getItems().size());
        assertEquals(3, cart.getItems().get(0).getQuantity());
        assertEquals(3, response.itemCount());
    }

    @Test
    void addRejectsOutOfStockProduct() {
        Customer customer = customer(1L);
        Cart cart = emptyCart(1L);
        when(securityUtils.currentUser()).thenReturn(customer);
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product(10L, 0, true)));

        assertThrows(BadRequestException.class, () -> service.add(new AddToCartRequest(10L, 1)));
        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void addRejectsExceedingStock() {
        Customer customer = customer(1L);
        Cart cart = emptyCart(1L);
        addItem(cart, 10L, 9);
        when(securityUtils.currentUser()).thenReturn(customer);
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product(10L, 10, true)));

        assertThrows(BadRequestException.class, () -> service.add(new AddToCartRequest(10L, 2)));
        assertEquals(9, cart.getItems().get(0).getQuantity());
    }

    @Test
    void removeProductDropsItFromCollection() {
        Customer customer = customer(1L);
        Cart cart = emptyCart(1L);
        addItem(cart, 10L, 2);
        addItem(cart, 11L, 1);
        when(securityUtils.currentUser()).thenReturn(customer);
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));

        CartResponse response = service.remove(10L);

        assertEquals(1, cart.getItems().size());
        assertEquals(11L, cart.getItems().get(0).getId().getProductId());
        assertEquals(1, response.itemCount());
    }

    @Test
    void removeMissingProductThrows() {
        Customer customer = customer(1L);
        Cart cart = emptyCart(1L);
        addItem(cart, 11L, 1);
        when(securityUtils.currentUser()).thenReturn(customer);
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));

        assertThrows(ResourceNotFoundException.class, () -> service.remove(10L));
        assertEquals(1, cart.getItems().size());
    }

    @Test
    void updateQuantityToZeroRemovesItem() {
        Customer customer = customer(1L);
        Cart cart = emptyCart(1L);
        addItem(cart, 10L, 1);
        when(securityUtils.currentUser()).thenReturn(customer);
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));

        CartResponse response = service.updateQuantity(10L, 0);

        assertTrue(cart.getItems().isEmpty());
        assertEquals(0, response.itemCount());
    }

    @Test
    void updateQuantityAdjustsCollectionValue() {
        Customer customer = customer(1L);
        Cart cart = emptyCart(1L);
        addItem(cart, 10L, 1);
        when(securityUtils.currentUser()).thenReturn(customer);
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));

        CartResponse response = service.updateQuantity(10L, 5);

        assertEquals(5, cart.getItems().get(0).getQuantity());
        assertEquals(5, response.itemCount());
    }
}