package com.ecomart.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @Size(min = 3, max = 50) String username,
        @Pattern(regexp = "^[0-9+ ]{10,15}$", message = "Số điện thoại không hợp lệ") String numberPhone,
        @Size(max = 2000) String avatarUrl,
        String currentPassword,
        @Size(min = 6, max = 100) String newPassword
) {
}
