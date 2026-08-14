package com.ecomart.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CategoryRequest {
    public Long parentId;
    @NotBlank public String name;
    @NotBlank public String slug;
    public String icon;
    public Integer displayOrder;
    @NotNull public Boolean active;
}
