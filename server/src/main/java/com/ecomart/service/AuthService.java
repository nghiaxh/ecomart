package com.ecomart.service;

import com.ecomart.common.Mapper;
import com.ecomart.common.SecurityUtils;
import com.ecomart.config.JwtProperties;
import com.ecomart.config.PayOSProperties;
import com.ecomart.config.GeminiProperties;
import com.ecomart.domain.entity.*;
import com.ecomart.domain.enums.ChatRole;
import com.ecomart.domain.enums.MaterialType;
import com.ecomart.domain.enums.NotificationType;
import com.ecomart.domain.enums.PointTransactionType;
import com.ecomart.domain.enums.UserRole;
import com.ecomart.dto.request.*;
import com.ecomart.dto.response.*;
import com.ecomart.exception.BadRequestException;
import com.ecomart.exception.ResourceNotFoundException;
import com.ecomart.repository.*;
import com.ecomart.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final CartRepository cartRepository;
    private final EcoWalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       CustomerRepository customerRepository,
                       CartRepository cartRepository,
                       EcoWalletRepository walletRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.cartRepository = cartRepository;
        this.walletRepository = walletRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email đã được sử dụng");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new BadRequestException("Tên đăng nhập đã tồn tại");
        }

        Customer customer = new Customer();
        customer.setUsername(request.username());
        customer.setEmail(request.email());
        customer.setNumberPhone(request.numberPhone());
        customer.setPasswordHash(passwordEncoder.encode(request.password()));
        customer.setRole(UserRole.CUSTOMER);
        customer.setActive(true);
        customer = customerRepository.save(customer);

        Cart cart = new Cart();
        cart.setCustomer(customer);
        cart = cartRepository.save(cart);
        customer.setCart(cart);
        customerRepository.save(customer);

        EcoWallet wallet = new EcoWallet();
        wallet.setCustomer(customer);
        walletRepository.save(wallet);

        String token = jwtTokenProvider.generateToken(customer.getId(), customer.getRole().name());
        return Mapper.toAuth(customer, token);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        org.springframework.security.core.userdetails.User principal =
                (org.springframework.security.core.userdetails.User) auth.getPrincipal();
        String email = principal.getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Email hoặc mật khẩu không đúng"));
        String token = jwtTokenProvider.generateToken(user.getId(), user.getRole().name());
        return Mapper.toAuth(user, token);
    }
}
