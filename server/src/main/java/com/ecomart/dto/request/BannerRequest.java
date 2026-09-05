package com.ecomart.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BannerRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 200) String subtitle,
        @NotBlank @Pattern(regexp = "^(https?://|/).*", message = "Đường dẫn ảnh không hợp lệ") String imageUrl,
        @Pattern(regexp = "^(https?://|/|#).*", message = "Đường dẫn liên kết không hợp lệ") String linkUrl,
        Integer displayOrder,
        Boolean active) {
}
