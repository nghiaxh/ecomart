package com.ecomart.service;

import com.ecomart.common.Mapper;
import com.ecomart.domain.entity.Admin;
import com.ecomart.domain.entity.Cart;
import com.ecomart.domain.entity.Customer;
import com.ecomart.domain.entity.User;
import com.ecomart.domain.enums.UserRole;
import com.ecomart.dto.request.CreateUserRequest;
import com.ecomart.dto.request.UpdateUserRequest;
import com.ecomart.dto.response.PageResponse;
import com.ecomart.dto.response.UserSummaryResponse;
import com.ecomart.exception.BadRequestException;
import com.ecomart.exception.ResourceNotFoundException;
import com.ecomart.repository.CartRepository;
import com.ecomart.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    public AdminUserService(UserRepository userRepository,
                            CartRepository cartRepository,
                            PasswordEncoder passwordEncoder,
                            EntityManager entityManager) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.passwordEncoder = passwordEncoder;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserSummaryResponse> listUsers(int page, int size, String search) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> users;
        if (search != null && !search.isBlank()) {
            users = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    search.trim(), search.trim(), pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        List<UserSummaryResponse> content = users.getContent().stream()
                .map(Mapper::toUserSummary)
                .toList();
        return new PageResponse<>(content, users.getNumber(), users.getSize(),
                users.getTotalElements(), users.getTotalPages());
    }

    @Transactional
    public UserSummaryResponse createUser(CreateUserRequest request) {
        UserRole role = request.role() == null ? UserRole.CUSTOMER : request.role();
        assertUnique(request.username(), request.email());
        User user = role == UserRole.ADMIN
                ? buildAdmin(request, LocalDate.now())
                : buildCustomer(request, LocalDate.now());
        return Mapper.toUserSummary(userRepository.save(user));
    }

    @Transactional
    public UserSummaryResponse updateUser(Long id, UpdateUserRequest request, Long currentUserId) {
        assertUnique(id, request.username(), request.email());
        User user = getById(id);

        UserRole newRole = request.role();
        if (newRole == null) {
            throw new BadRequestException("Invalid role");
        }
        if (id.equals(currentUserId) && newRole != user.getRole()) {
            throw new BadRequestException("Cannot change your own role");
        }

        boolean sameRole = user.getRole() == newRole;
        if (sameRole) {
            applyCommonFields(user, request);
            if (request.password() != null && !request.password().isBlank()) {
                user.setPasswordHash(passwordEncoder.encode(request.password()));
            }
            if (user instanceof Admin admin && request.hireDate() != null) {
                admin.setHireDate(request.hireDate());
            }
            return Mapper.toUserSummary(userRepository.save(user));
        }

        return Mapper.toUserSummary(switchRole(user, request));
    }

    @Transactional
    public UserSummaryResponse toggleActive(Long id, Long currentUserId) {
        User user = getById(id);
        if (id.equals(currentUserId)) {
            throw new BadRequestException("Cannot deactivate your own account");
        }
        user.setActive(!user.isActive());
        return Mapper.toUserSummary(userRepository.save(user));
    }

    private User switchRole(User user, UpdateUserRequest request) {
        userRepository.delete(user);
        entityManager.flush();

        UserRole role = request.role();
        User replacement;
        if (role == UserRole.ADMIN) {
            Admin admin = new Admin();
            applyCommonFields(admin, request);
            admin.setHireDate(request.hireDate() != null ? request.hireDate() : LocalDate.now());
            if (request.password() != null && !request.password().isBlank()) {
                admin.setPasswordHash(passwordEncoder.encode(request.password()));
            }
            replacement = admin;
        } else {
            Customer customer = new Customer();
            applyCommonFields(customer, request);
            if (request.password() != null && !request.password().isBlank()) {
                customer.setPasswordHash(passwordEncoder.encode(request.password()));
            }
            Cart cart = new Cart();
            cart.setCustomer(customer);
            customer.setCart(cart);
            cartRepository.save(cart);
            replacement = customer;
        }
        User saved = userRepository.save(replacement);
        entityManager.flush();
        entityManager.clear();
        return getById(saved.getId());
    }

    private void applyCommonFields(User user, UpdateUserRequest request) {
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setNumberPhone(request.numberPhone());
        user.setRole(request.role());
    }

    private Admin buildAdmin(CreateUserRequest request, LocalDate hireDate) {
        Admin admin = new Admin();
        admin.setUsername(request.username());
        admin.setEmail(request.email());
        admin.setNumberPhone(request.numberPhone());
        admin.setPasswordHash(passwordEncoder.encode(request.password()));
        admin.setRole(UserRole.ADMIN);
        admin.setActive(true);
        admin.setHireDate(request.hireDate() != null ? request.hireDate() : hireDate);
        return admin;
    }

    private Customer buildCustomer(CreateUserRequest request, LocalDate unused) {
        Customer customer = new Customer();
        customer.setUsername(request.username());
        customer.setEmail(request.email());
        customer.setNumberPhone(request.numberPhone());
        customer.setPasswordHash(passwordEncoder.encode(request.password()));
        customer.setRole(UserRole.CUSTOMER);
        customer.setActive(true);
        Cart cart = new Cart();
        cart.setCustomer(customer);
        customer.setCart(cart);
        cartRepository.save(cart);
        return customer;
    }

    private void assertUnique(String username, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new BadRequestException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already exists");
        }
    }

    private void assertUnique(Long id, String username, String email) {
        userRepository.findByUsername(username)
                .filter(u -> !u.getId().equals(id))
                .ifPresent(u -> {
                    throw new BadRequestException("Username already exists");
                });
        userRepository.findByEmail(email)
                .filter(u -> !u.getId().equals(id))
                .ifPresent(u -> {
                    throw new BadRequestException("Email already exists");
                });
    }

    private User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
