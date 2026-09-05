package com.ecomart.service;

import com.ecomart.common.Mapper;
import com.ecomart.common.SecurityUtils;
import com.ecomart.domain.entity.User;
import com.ecomart.dto.request.ProfileUpdateRequest;
import com.ecomart.dto.response.ProfileResponse;
import com.ecomart.exception.BadRequestException;
import com.ecomart.exception.ResourceNotFoundException;
import com.ecomart.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileService(SecurityUtils securityUtils, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.securityUtils = securityUtils;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public ProfileResponse getProfile() {
        User user = securityUtils.currentUser();
        return Mapper.toProfile(user);
    }

    @Transactional
    public ProfileResponse update(ProfileUpdateRequest request) {
        User user = securityUtils.currentUser();
        if (request.username() != null && !request.username().isBlank()
                && !request.username().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.username())) {
                throw new BadRequestException("Tên đăng nhập đã tồn tại");
            }
            user.setUsername(request.username());
        }
        if (request.numberPhone() != null && !request.numberPhone().isBlank()) {
            user.setNumberPhone(request.numberPhone());
        }
        if (request.avatarUrl() != null) {
            user.setAvatarUrl(request.avatarUrl());
        }
        if (request.newPassword() != null && !request.newPassword().isBlank()) {
            if (request.currentPassword() == null || !passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
                throw new BadRequestException("Mật khẩu hiện tại không đúng");
            }
            user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        }
        return getProfile();
    }
}
