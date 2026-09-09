package com.ecomart.controller;

import com.ecomart.common.SecurityUtils;
import com.ecomart.dto.request.CreateUserRequest;
import com.ecomart.dto.request.UpdateUserRequest;
import com.ecomart.dto.response.AdminDashboardResponse;
import com.ecomart.dto.response.AdminStatisticsResponse;
import com.ecomart.dto.response.PageResponse;
import com.ecomart.dto.response.UserSummaryResponse;
import com.ecomart.service.AdminStatsService;
import com.ecomart.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminStatsService adminStatsService;
    private final AdminUserService adminUserService;
    private final SecurityUtils securityUtils;

    public AdminController(AdminStatsService adminStatsService,
                           AdminUserService adminUserService,
                           SecurityUtils securityUtils) {
        this.adminStatsService = adminStatsService;
        this.adminUserService = adminUserService;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/dashboard")
    public AdminDashboardResponse dashboard() {
        return adminStatsService.dashboard();
    }

    @GetMapping("/statistics")
    public AdminStatisticsResponse statistics(@RequestParam(defaultValue = "30") int days) {
        return adminStatsService.statistics(days);
    }

    @GetMapping("/users")
    public PageResponse<UserSummaryResponse> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        return adminUserService.listUsers(page, size, search);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserSummaryResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        return adminUserService.createUser(request);
    }

    @PutMapping("/users/{id}")
    public UserSummaryResponse updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return adminUserService.updateUser(id, request, securityUtils.currentUserId());
    }

    @PatchMapping("/users/{id}/toggle-active")
    public UserSummaryResponse toggleActive(@PathVariable Long id) {
        return adminUserService.toggleActive(id, securityUtils.currentUserId());
    }
}
