package com.ecomart.service;

import com.ecomart.common.Mapper;
import com.ecomart.domain.entity.Admin;
import com.ecomart.domain.entity.User;
import com.ecomart.domain.enums.UserRole;
import com.ecomart.dto.request.CreateAdminRequest;
import com.ecomart.dto.response.PageResponse;
import com.ecomart.dto.response.UserSummaryResponse;
import com.ecomart.exception.BadRequestException;
import com.ecomart.exception.ResourceNotFoundException;
import com.ecomart.repository.UserRepository;
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
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
    public UserSummaryResponse createAdmin(CreateAdminRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BadRequestException("Tên đăng nhập đã tồn tại");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email đã tồn tại");
        }
        Admin admin = new Admin();
        admin.setUsername(request.username());
        admin.setEmail(request.email());
        admin.setNumberPhone(request.numberPhone());
        admin.setPasswordHash(passwordEncoder.encode(request.password()));
        admin.setRole(UserRole.ADMIN);
        admin.setActive(true);
        admin.setHireDate(request.hireDate() != null ? request.hireDate() : LocalDate.now());
        return Mapper.toUserSummary(userRepository.save(admin));
    }

    @Transactional
    public UserSummaryResponse toggleActive(Long id) {
        User user = getById(id);
        user.setActive(!user.isActive());
        return Mapper.toUserSummary(userRepository.save(user));
    }

    private User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
    }
}
