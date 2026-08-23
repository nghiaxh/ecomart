package com.ecomart.service;

import com.ecomart.common.Mapper;
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
import com.ecomart.repository.CartItemRepository;
import com.ecomart.repository.CartRepository;
import com.ecomart.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final SecurityUtils securityUtils;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(SecurityUtils securityUtils,
                       CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductRepository productRepository) {
        this.securityUtils = securityUtils;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    public CartResponse getMyCart() {
        return Mapper.toCart(getCart());
    }

    @Transactional
    public CartResponse add(AddToCartRequest request) {
        Cart cart = getCart();
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));
        if (!product.isActive()) {
            throw new BadRequestException("Sản phẩm đã ngừng kinh doanh");
        }
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .orElseGet(() -> {
                    CartItem ci = new CartItem();
                    ci.setId(new CartItemId(cart.getId(), product.getId()));
                    ci.setCart(cart);
                    ci.setProduct(product);
                    ci.setQuantity(0);
                    return ci;
                });
        int newQuantity = item.getQuantity() + request.quantity();
        if (newQuantity > product.getStock()) {
            throw new BadRequestException("Số lượng vượt quá tồn kho");
        }
        item.setQuantity(newQuantity);
        cartItemRepository.save(item);
        return Mapper.toCart(getCart());
    }

    @Transactional
    public CartResponse updateQuantity(Long productId, int quantity) {
        Cart cart = getCart();
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không có trong giỏ hàng"));
        Product product = item.getProduct();
        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            if (quantity > product.getStock()) {
                throw new BadRequestException("Số lượng vượt quá tồn kho");
            }
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }
        return Mapper.toCart(getCart());
    }

    @Transactional
    public CartResponse remove(Long productId) {
        Cart cart = getCart();
        cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .ifPresent(cartItemRepository::delete);
        return Mapper.toCart(getCart());
    }

    public Cart getCart() {
        Customer customer = (Customer) securityUtils.currentUser();
        return cartRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giỏ hàng"));
    }
}
