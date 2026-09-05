package com.ecomart.service;

import com.ecomart.common.Mapper;
import com.ecomart.common.SecurityUtils;
import com.ecomart.config.JwtProperties;
import com.ecomart.config.PayOSProperties;
import com.ecomart.domain.entity.*;
import com.ecomart.domain.enums.ChatRole;
import com.ecomart.domain.enums.MaterialType;
import com.ecomart.domain.enums.NotificationType;
import com.ecomart.domain.enums.UserRole;
import com.ecomart.dto.request.*;
import com.ecomart.dto.response.*;
import com.ecomart.exception.BadRequestException;
import com.ecomart.exception.ResourceNotFoundException;
import com.ecomart.exception.UnauthorizedException;
import com.ecomart.repository.*;
import com.ecomart.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final CartRepository cartRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       CustomerRepository customerRepository,
                       CartRepository cartRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       JwtProperties jwtProperties,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.cartRepository = cartRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
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

        return issueTokenPair(customer);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.identifier(), request.password()));
        org.springframework.security.core.userdetails.User principal =
                (org.springframework.security.core.userdetails.User) auth.getPrincipal();
        String email = principal.getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Email hoặc mật khẩu không đúng"));
        return issueTokenPair(user);
    }

    @Transactional(noRollbackFor = UnauthorizedException.class)
    public AuthResponse refresh(RefreshTokenRequest request) {
        String hash = JwtTokenProvider.sha256Hex(request.refreshToken().trim());
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new UnauthorizedException("Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại"));
        if (stored.getRevokedAt() != null) {
            revokeAllForUser(stored.getUser().getId());
            throw new UnauthorizedException("Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại");
        }
        if (stored.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(stored);
            throw new UnauthorizedException("Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại");
        }
        User user = stored.getUser();
        if (!user.isActive()) {
            throw new UnauthorizedException("Tài khoản đã bị khóa");
        }
        stored.setRevokedAt(Instant.now());
        AuthResponse response = issueTokenPair(user);
        stored.setReplacedBy(JwtTokenProvider.sha256Hex(response.refreshToken()));
        return response;
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        String hash = JwtTokenProvider.sha256Hex(request.refreshToken().trim());
        refreshTokenRepository.findByTokenHash(hash).ifPresent(stored -> {
            if (stored.getRevokedAt() == null) {
                stored.setRevokedAt(Instant.now());
            }
        });
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgeExpiredRefreshTokens() {
        refreshTokenRepository.deleteByExpiresAtBefore(Instant.now().minusSeconds(7 * 24 * 3600));
    }

    private AuthResponse issueTokenPair(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getRole().name());
        String refreshValue = jwtTokenProvider.generateRefreshTokenValue();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(JwtTokenProvider.sha256Hex(refreshValue));
        refreshToken.setExpiresAt(Instant.now().plusMillis(jwtProperties.refreshExpirationMs()));
        refreshTokenRepository.save(refreshToken);
        return Mapper.toAuth(user, accessToken, refreshValue, jwtProperties.accessExpirationMs() / 1000);
    }

    private void revokeAllForUser(Long userId) {
        Instant now = Instant.now();
        for (RefreshToken active : refreshTokenRepository.findByUserIdAndRevokedAtIsNull(userId)) {
            active.setRevokedAt(now);
        }
    }
}
