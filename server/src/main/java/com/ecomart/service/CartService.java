package com.ecomart.service;

import com.ecomart.common.Mapper;
import com.ecomart.common.SecurityUtils;
import com.ecomart.domain.entity.Cart;
import com.ecomart.domain.entity.CartItem;
import com.ecomart.domain.entity.CartItemId;
import com.ecomart.domain.entity.Customer;
import com.ecomart.domain.entity.Product;
import com.ecomart.domain.entity.User;
import com.ecomart.dto.request.AddToCartRequest;
import com.ecomart.dto.response.CartResponse;
import com.ecomart.exception.BadRequestException;
import com.ecomart.exception.ResourceNotFoundException;
import com.ecomart.repository.CartRepository;
import com.ecomart.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final SecurityUtils securityUtils;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartService(SecurityUtils securityUtils,
                       CartRepository cartRepository,
                       ProductRepository productRepository) {
        this.securityUtils = securityUtils;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public CartResponse getMyCart() {
        return Mapper.toCart(getCart());
    }

    @Transactional
    public CartResponse add(AddToCartRequest request) {
        Cart cart = getCart();
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        if (!product.isActive()) {
            throw new BadRequestException("Product has been discontinued");
        }
        if (product.getStock() == 0) {
            throw new BadRequestException("Product is out of stock");
        }
        cart.getItems().size();
        CartItem item = cart.getItems().stream()
                .filter(ci -> ci.getId().getProductId().equals(product.getId()))
                .findFirst()
                .orElseGet(() -> {
                    CartItem ci = new CartItem();
                    ci.setId(new CartItemId(cart.getId(), product.getId()));
                    ci.setCart(cart);
                    ci.setProduct(product);
                    ci.setQuantity(0);
                    cart.getItems().add(ci);
                    return ci;
                });
        int newQuantity = item.getQuantity() + request.quantity();
        if (newQuantity > product.getStock()) {
            throw new BadRequestException("Quantity exceeds available stock, only " + product.getStock() + " left");
        }
        item.setQuantity(newQuantity);
        return Mapper.toCart(cart);
    }

    @Transactional
    public CartResponse updateQuantity(Long productId, int quantity) {
        Cart cart = getCart();
        cart.getItems().size();
        CartItem item = cart.getItems().stream()
                .filter(ci -> ci.getId().getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Product is not in the cart"));
        if (quantity <= 0) {
            cart.getItems().remove(item);
            return Mapper.toCart(cart);
        }
        Product product = item.getProduct();
        if (product.getStock() == 0) {
            throw new BadRequestException("Product is out of stock");
        }
        if (quantity > product.getStock()) {
            throw new BadRequestException("Quantity exceeds available stock, only " + product.getStock() + " left");
        }
        item.setQuantity(quantity);
        return Mapper.toCart(cart);
    }

    @Transactional
    public CartResponse remove(Long productId) {
        Cart cart = getCart();
        boolean removed = cart.getItems().removeIf(ci -> ci.getId().getProductId().equals(productId));
        if (!removed) {
            throw new ResourceNotFoundException("Product is not in the cart");
        }
        return Mapper.toCart(cart);
    }

    public Cart getCart() {
        User user = securityUtils.currentUser();
        if (user instanceof Customer customer) {
            return cartRepository.findByCustomerId(customer.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        }
        throw new org.springframework.security.access.AccessDeniedException("Only customers have carts");
    }
}