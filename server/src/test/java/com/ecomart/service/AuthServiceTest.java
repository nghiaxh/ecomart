package com.ecomart.service;

import com.ecomart.config.JwtProperties;
import com.ecomart.domain.entity.Cart;
import com.ecomart.domain.entity.Customer;
import com.ecomart.domain.entity.RefreshToken;
import com.ecomart.domain.enums.UserRole;
import com.ecomart.dto.request.LoginRequest;
import com.ecomart.dto.request.RefreshTokenRequest;
import com.ecomart.dto.request.RegisterRequest;
import com.ecomart.dto.response.AuthResponse;
import com.ecomart.exception.BadRequestException;
import com.ecomart.exception.UnauthorizedException;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

    private Customer activeCustomer(long id, String email) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setUsername("minh");
        customer.setEmail(email);
        customer.setRole(UserRole.CUSTOMER);
        customer.setActive(true);
        return customer;
    }

    private RefreshToken refreshToken(long userId) {
        RefreshToken token = new RefreshToken();
        token.setUser(activeCustomer(userId, "minh@example.com"));
        token.setTokenHash(JwtTokenProvider.sha256Hex("some-value"));
        token.setExpiresAt(Instant.now().plusSeconds(3600));
        return token;
    }

    private void stubTokenPair(String accessToken, String refreshValue) {
        when(jwtProperties.accessExpirationMs()).thenReturn(3600000L);
        when(jwtProperties.refreshExpirationMs()).thenReturn(10800000L);
        when(jwtTokenProvider.generateAccessToken(any(), anyString())).thenReturn(accessToken);
        when(jwtTokenProvider.generateRefreshTokenValue()).thenReturn(refreshValue);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
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

    @Test
    void registerCreatesCustomerWithCartAndReturnsTokens() {
        when(userRepository.existsByEmail("minh@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("minh")).thenReturn(false);
        when(passwordEncoder.encode("secret1")).thenReturn("encoded");
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));
        stubTokenPair("access-token", "refresh-value");

        AuthResponse response = service.register(request());

        assertEquals("access-token", response.token());
        assertEquals("refresh-value", response.refreshToken());
        assertEquals(3600, response.expiresIn());
        assertEquals(UserRole.CUSTOMER, response.role());
        verify(customerRepository, times(2)).save(any(Customer.class));
        verify(cartRepository).save(any(Cart.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void loginReturnsTokenPairForValidCredentials() {
        Authentication authentication = mock(Authentication.class);
        org.springframework.security.core.userdetails.User principal =
                new org.springframework.security.core.userdetails.User("minh@example.com", "encoded", List.of());
        when(authentication.getPrincipal()).thenReturn(principal);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userRepository.findByEmail("minh@example.com"))
                .thenReturn(Optional.of(activeCustomer(1L, "minh@example.com")));
        stubTokenPair("access-token", "refresh-value");

        AuthResponse response = service.login(new LoginRequest("minh@example.com", "secret1"));

        assertEquals("minh@example.com", response.email());
        assertEquals("minh", response.username());
        assertEquals("refresh-value", response.refreshToken());
    }

    @Test
    void loginThrowsBadRequestWhenAccountMissingAfterAuth() {
        Authentication authentication = mock(Authentication.class);
        org.springframework.security.core.userdetails.User principal =
                new org.springframework.security.core.userdetails.User("ghost@example.com", "encoded", List.of());
        when(authentication.getPrincipal()).thenReturn(principal);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class,
                () -> service.login(new LoginRequest("ghost@example.com", "x")));
    }

    @Test
    void refreshThrowsWhenTokenNotFound() {
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());
        assertThrows(UnauthorizedException.class,
                () -> service.refresh(new RefreshTokenRequest("missing")));
    }

    @Test
    void refreshRevokedTokenRevokesAllActiveTokens() {
        RefreshToken stored = refreshToken(1L);
        stored.setRevokedAt(Instant.now());
        RefreshToken otherActive = refreshToken(1L);
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(stored));
        when(refreshTokenRepository.findByUserIdAndRevokedAtIsNull(1L)).thenReturn(List.of(otherActive));

        assertThrows(UnauthorizedException.class,
                () -> service.refresh(new RefreshTokenRequest("used")));

        verify(refreshTokenRepository).findByUserIdAndRevokedAtIsNull(1L);
        assertNotNull(otherActive.getRevokedAt());
    }

    @Test
    void refreshExpiredTokenIsDeletedAndThrows() {
        RefreshToken stored = refreshToken(1L);
        stored.setRevokedAt(null);
        stored.setExpiresAt(Instant.now().minusSeconds(10));
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(stored));

        assertThrows(UnauthorizedException.class,
                () -> service.refresh(new RefreshTokenRequest("expired")));

        verify(refreshTokenRepository).delete(stored);
    }

    @Test
    void refreshInactiveUserThrowsLocked() {
        RefreshToken stored = refreshToken(1L);
        stored.setRevokedAt(null);
        stored.getUser().setActive(false);
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(stored));

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> service.refresh(new RefreshTokenRequest("locked")));

        assertEquals("Account has been locked", ex.getMessage());
    }

    @Test
    void refreshRotatesTokenAndLinksReplacedBy() {
        RefreshToken stored = refreshToken(1L);
        stored.setRevokedAt(null);
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(stored));
        stubTokenPair("new-access", "new-refresh");

        AuthResponse response = service.refresh(new RefreshTokenRequest("old"));

        assertNotNull(stored.getRevokedAt());
        assertEquals(JwtTokenProvider.sha256Hex("new-refresh"), stored.getReplacedBy());
        assertEquals("new-access", response.token());
        assertEquals("new-refresh", response.refreshToken());
    }
}