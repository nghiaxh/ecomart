package com.ecomart.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class BannerRequest {
    @NotBlank @Size(max = 200) public String title;
    @Size(max = 200) public String subtitle;
    @NotBlank @Pattern(regexp = "^(https?://|/).*", message = "Đường dẫn ảnh không hợp lệ") public String imageUrl;
    @Pattern(regexp = "^(https?://|/|#).*", message = "Đường dẫn liên kết không hợp lệ") public String linkUrl;
    public Integer displayOrder;
    public Boolean active;
}
