package com.ecomart.service;

import com.ecomart.config.JwtProperties;
import com.ecomart.dto.request.RegisterRequest;
import com.ecomart.exception.BadRequestException;
import com.ecomart.repository.CartRepository;
import com.ecomart.repository.CustomerRepository;
import com.ecomart.repository.RefreshTokenRepository;
import com.ecomart.repository.UserRepository;
import com.ecomart.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock CustomerRepository customerRepository;
    @Mock CartRepository cartRepository;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtTokenProvider jwtTokenProvider;
    @Mock JwtProperties jwtProperties;
    @Mock AuthenticationManager authenticationManager;

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(userRepository, customerRepository, cartRepository,
                refreshTokenRepository, passwordEncoder, jwtTokenProvider, jwtProperties,
                authenticationManager);
    }

    private RegisterRequest request() {
        return new RegisterRequest("minh", "minh@example.com", "0901234567", "secret1");
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userRepository.existsByEmail("minh@example.com")).thenReturn(true);
        assertThrows(BadRequestException.class, () -> service.register(request()));
    }

    @Test
    void registerRejectsDuplicateUsername() {
        when(userRepository.existsByEmail("minh@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("minh")).thenReturn(true);
        assertThrows(BadRequestException.class, () -> service.register(request()));
    }
}