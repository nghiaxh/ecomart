package com.ecomart.security;

import com.ecomart.domain.entity.Customer;
import com.ecomart.domain.enums.UserRole;
import com.ecomart.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock UserRepository userRepository;

    private UserDetailsServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserDetailsServiceImpl(userRepository);
    }

    private Customer customer(String email, boolean active) {
        Customer c = new Customer();
        c.setId(1L);
        c.setUsername("minh");
        c.setEmail(email);
        c.setPasswordHash("encoded");
        c.setRole(UserRole.CUSTOMER);
        c.setActive(active);
        return c;
    }

    @Test
    void loadUserByUsernameFallsBackToEmailLookup() {
        when(userRepository.findByUsername("minh@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("minh@example.com")).thenReturn(Optional.of(customer("minh@example.com", true)));

        UserDetails details = service.loadUserByUsername("minh@example.com");

        assertEquals("minh@example.com", details.getUsername());
        assertTrue(details.isEnabled());
        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER")));
    }

    @Test
    void disabledAccountReportsNotEnabled() {
        when(userRepository.findByUsername("minh")).thenReturn(Optional.of(customer("minh@example.com", false)));

        UserDetails details = service.loadUserByUsername("minh");

        assertFalse(details.isEnabled());
    }
}