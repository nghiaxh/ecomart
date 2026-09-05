package com.ecomart.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @Size(min = 3, max = 50) String username,
        @Pattern(regexp = "^$|^(0|\\+84)[0-9]{9,10}$", message = "Số điện thoại không hợp lệ") String numberPhone,
        @Size(max = 2000) String avatarUrl,
        String currentPassword,
        @Pattern(regexp = "^$|^.{6,100}$", message = "Mật khẩu mới phải có ít nhất 6 ký tự") String newPassword
) {
}
