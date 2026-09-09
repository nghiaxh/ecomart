package com.ecomart.dto.request;

import com.ecomart.domain.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateUserRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Email String email,
        @NotBlank @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Invalid phone number") String numberPhone,
        @NotNull UserRole role,
        @Size(min = 6, max = 100) String password,
        LocalDate hireDate
) {
}
