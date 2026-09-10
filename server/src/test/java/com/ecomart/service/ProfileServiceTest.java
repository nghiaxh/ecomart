package com.ecomart.service;

import com.ecomart.common.SecurityUtils;
import com.ecomart.domain.entity.Customer;
import com.ecomart.domain.enums.UserRole;
import com.ecomart.dto.request.ProfileUpdateRequest;
import com.ecomart.dto.response.ProfileResponse;
import com.ecomart.exception.BadRequestException;
import com.ecomart.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock SecurityUtils securityUtils;
    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;

    private ProfileService service;

    @BeforeEach
    void setUp() {
        service = new ProfileService(securityUtils, userRepository, passwordEncoder);
    }

    private Customer user(String username, String phone, String passwordHash) {
        Customer u = new Customer();
        u.setId(1L);
        u.setUsername(username);
        u.setEmail("minh@example.com");
        u.setNumberPhone(phone);
        u.setRole(UserRole.CUSTOMER);
        u.setPasswordHash(passwordHash);
        return u;
    }

    @Test
    void updateProfileAppliesChangedFields() {
        Customer user = user("minh", "0901234567", "encoded");
        when(securityUtils.currentUser()).thenReturn(user);

        ProfileResponse response = service.update(
                new ProfileUpdateRequest("minh", "0912345678", "https://img/avatar.png", null, null));

        assertEquals("minh", response.username());
        assertEquals("0912345678", response.numberPhone());
        assertEquals("https://img/avatar.png", response.avatarUrl());
    }

    @Test
    void updateRejectsDuplicateUsername() {
        Customer user = user("minh", "0901234567", "encoded");
        when(securityUtils.currentUser()).thenReturn(user);
        when(userRepository.existsByUsername("khac")).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.update(new ProfileUpdateRequest("khac", null, null, null, null)));

        assertEquals("Username already exists", ex.getMessage());
    }

    @Test
    void updateRejectsWrongCurrentPassword() {
        Customer user = user("minh", "0901234567", "encoded");
        when(securityUtils.currentUser()).thenReturn(user);
        when(passwordEncoder.matches("sai", "encoded")).thenReturn(false);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.update(new ProfileUpdateRequest(null, null, null, "sai", "newpass1")));

        assertEquals("Current password is incorrect", ex.getMessage());
    }

    @Test
    void updateChangesPasswordWhenCurrentMatches() {
        Customer user = user("minh", "0901234567", "encoded");
        when(securityUtils.currentUser()).thenReturn(user);
        when(passwordEncoder.matches("cu", "encoded")).thenReturn(true);
        when(passwordEncoder.encode("moi")).thenReturn("new-encoded");

        service.update(new ProfileUpdateRequest(null, null, null, "cu", "moi"));

        assertEquals("new-encoded", user.getPasswordHash());
    }
}