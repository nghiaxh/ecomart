package com.ecomart.dto.request;

import jakarta.validation.constraints.NotBlank;

public class BannerRequest {
    @NotBlank public String title;
    public String subtitle;
    @NotBlank public String imageUrl;
    public String linkUrl;
    public Integer displayOrder;
    public Boolean active;
}
