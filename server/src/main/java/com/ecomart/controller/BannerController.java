package com.ecomart.controller;

import com.ecomart.dto.request.BannerRequest;
import com.ecomart.dto.response.BannerResponse;
import com.ecomart.dto.response.MessageResponse;
import com.ecomart.service.BannerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/banners")
public class BannerController {

    private final BannerService bannerService;

    public BannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    @GetMapping("/active")
    public List<BannerResponse> active() {
        return bannerService.active();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<BannerResponse> all() {
        return bannerService.all();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public BannerResponse create(@Valid @RequestBody BannerRequest request) {
        return bannerService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public BannerResponse update(@PathVariable Long id, @Valid @RequestBody BannerRequest request) {
        return bannerService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public MessageResponse delete(@PathVariable Long id) {
        bannerService.delete(id);
        return new MessageResponse("Đã xóa banner");
    }
}
