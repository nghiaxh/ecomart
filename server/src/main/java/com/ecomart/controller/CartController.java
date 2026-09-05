package com.ecomart.controller;

import com.ecomart.dto.request.AddToCartRequest;
import com.ecomart.dto.response.CartResponse;
import com.ecomart.dto.response.MessageResponse;
import com.ecomart.service.CartService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@PreAuthorize("isAuthenticated()")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartResponse getMyCart() {
        return cartService.getMyCart();
    }

    @PostMapping
    public CartResponse add(@Valid @RequestBody AddToCartRequest request) {
        return cartService.add(request);
    }

    @PutMapping("/{productId}")
    public CartResponse updateQuantity(@PathVariable Long productId, @RequestParam int quantity) {
        return cartService.updateQuantity(productId, quantity);
    }

    @DeleteMapping("/{productId}")
    public CartResponse remove(@PathVariable Long productId) {
        return cartService.remove(productId);
    }
}
