package com.ecomart.service;

import com.ecomart.domain.entity.Admin;
import com.ecomart.domain.entity.Customer;
import com.ecomart.domain.entity.Staff;
import com.ecomart.domain.entity.User;
import com.ecomart.domain.enums.UserRole;
import com.ecomart.dto.request.CreateUserRequest;
import com.ecomart.dto.request.UpdateUserRequest;
import com.ecomart.dto.response.PageResponse;
import com.ecomart.dto.response.UserSummaryResponse;
import com.ecomart.exception.BadRequestException;
import com.ecomart.repository.CartRepository;
import com.ecomart.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock UserRepository userRepository;
    @Mock CartRepository cartRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock EntityManager entityManager;

    private AdminUserService service;

    @BeforeEach
    void setUp() {
        service = new AdminUserService(userRepository, cartRepository, passwordEncoder, entityManager,
                mock(ActivityLogService.class));
    }

    @Test
    void listUsersReturnsPageOfSummaries() {
        Admin admin = new Admin();
        admin.setId(1L);
        admin.setRole(UserRole.ADMIN);
        @SuppressWarnings("unchecked")
        org.springframework.data.domain.Page<User> page = mock(org.springframework.data.domain.Page.class);
        when(userRepository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(page);
        when(page.getContent()).thenReturn(java.util.List.<User>of(admin));
        when(page.getNumber()).thenReturn(0);
        when(page.getSize()).thenReturn(10);
        when(page.getTotalElements()).thenReturn(1L);
        when(page.getTotalPages()).thenReturn(1);

        PageResponse<UserSummaryResponse> result = service.listUsers(0, 10, null);

        assertEquals(1, result.content().size());
        assertEquals(UserRole.ADMIN, result.content().get(0).role());
    }

    @Test
    void createUserWithAdminRoleBuildsAdmin() {
        when(passwordEncoder.encode("secret")).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        CreateUserRequest req = new CreateUserRequest("quanly1", "q1@ecomart.vn", "0900000001",
                "secret", UserRole.ADMIN, LocalDate.of(2026, 1, 1));

        UserSummaryResponse result = service.createUser(req);

        assertEquals(UserRole.ADMIN, result.role());
        verify(userRepository).save(any(Admin.class));
        verify(cartRepository, never()).save(any());
    }

    @Test
    void createUserWithCustomerRoleBuildsCustomerWithCart() {
        when(passwordEncoder.encode("secret")).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        CreateUserRequest req = new CreateUserRequest("khach1", "k1@ecomart.vn", "0900000002",
                "secret", UserRole.CUSTOMER, null);

        UserSummaryResponse result = service.createUser(req);

        assertEquals(UserRole.CUSTOMER, result.role());
        verify(userRepository).save(any(Customer.class));
        verify(cartRepository).save(any());
    }

    @Test
    void createUserWithStaffRoleBuildsStaff() {
        when(passwordEncoder.encode("secret")).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        CreateUserRequest req = new CreateUserRequest("nhanvien1", "nv1@ecomart.vn", "0900000007",
                "secret", UserRole.STAFF, LocalDate.of(2026, 3, 1));

        UserSummaryResponse result = service.createUser(req);

        assertEquals(UserRole.STAFF, result.role());
        verify(userRepository).save(any(Staff.class));
        verify(cartRepository, never()).save(any());
    }

    @Test
    void switchCustomerToStaffRecreatesAsStaff() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUsername("khach1");
        customer.setRole(UserRole.CUSTOMER);
        java.util.concurrent.atomic.AtomicReference<User> savedRef = new java.util.concurrent.atomic.AtomicReference<>();
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(customer))
                .thenAnswer(inv -> Optional.ofNullable(savedRef.get()));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User saved = inv.getArgument(0);
            saved.setId(1L);
            savedRef.set(saved);
            return saved;
        });
        UpdateUserRequest req = new UpdateUserRequest("khach1", "k1@ecomart.vn", "0900000008",
                UserRole.STAFF, null, LocalDate.of(2026, 4, 1));

        UserSummaryResponse result = service.updateUser(1L, req, 99L);

        assertEquals(UserRole.STAFF, result.role());
        verify(userRepository).save(any(Staff.class));
        verify(userRepository).delete(customer);
        verify(cartRepository, never()).save(any());
    }

    @Test
    void createUserRejectsDuplicateUsername() {
        CreateUserRequest req = new CreateUserRequest("quanly1", "new@ecomart.vn", "0900000003",
                "secret", UserRole.CUSTOMER, null);
        when(userRepository.existsByUsername("quanly1")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> service.createUser(req));
    }

    @Test
    void updateSameRolePreservesRole() {
        Admin admin = new Admin();
        admin.setId(1L);
        admin.setUsername("quanly1");
        admin.setRole(UserRole.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        UpdateUserRequest req = new UpdateUserRequest("quanly1", "new@ecomart.vn", "0900000004",
                UserRole.ADMIN, null, LocalDate.of(2026, 2, 1));

        UserSummaryResponse result = service.updateUser(1L, req, 99L);

        assertEquals("new@ecomart.vn", result.email());
        assertEquals(UserRole.ADMIN, result.role());
    }

    @Test
    void updateChangeOwnRoleThrows() {
        Admin admin = new Admin();
        admin.setId(1L);
        admin.setRole(UserRole.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        UpdateUserRequest req = new UpdateUserRequest("quanly1", "a@ecomart.vn", "0900000005",
                UserRole.CUSTOMER, null, null);

        assertThrows(BadRequestException.class, () -> service.updateUser(1L, req, 1L));
        verify(userRepository, never()).save(any());
    }

    @Test
    void toggleActiveOnSelfThrows() {
        Admin admin = new Admin();
        admin.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThrows(BadRequestException.class, () -> service.toggleActive(1L, 1L));
        verify(userRepository, never()).save(any());
    }

    @Test
    void toggleActiveOnOtherToggles() {
        Admin admin = new Admin();
        admin.setId(2L);
        admin.setActive(true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(admin));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserSummaryResponse result = service.toggleActive(2L, 1L);

        assertTrue(!result.isActive());
    }
}
