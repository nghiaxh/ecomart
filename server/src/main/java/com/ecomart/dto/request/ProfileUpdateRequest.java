package com.ecomart.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @Size(min = 3, max = 50) String username,
        String numberPhone,
        @Size(max = 2000) String avatarUrl,
        String currentPassword,
        @Size(max = 100) String newPassword
) {
}
