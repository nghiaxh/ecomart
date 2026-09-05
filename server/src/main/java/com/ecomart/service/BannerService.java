package com.ecomart.service;

import com.ecomart.common.Mapper;
import com.ecomart.domain.entity.Banner;
import com.ecomart.dto.request.BannerRequest;
import com.ecomart.dto.response.BannerResponse;
import com.ecomart.exception.ResourceNotFoundException;
import com.ecomart.repository.BannerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BannerService {

    private final BannerRepository bannerRepository;

    public BannerService(BannerRepository bannerRepository) {
        this.bannerRepository = bannerRepository;
    }

    @Transactional(readOnly = true)
    public List<BannerResponse> active() {
        return bannerRepository.findByIsActiveTrueOrderByDisplayOrderAsc().stream()
                .map(Mapper::toBanner)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BannerResponse> all() {
        return bannerRepository.findAll().stream()
                .sorted((a, b) -> Integer.compare(a.getDisplayOrder(), b.getDisplayOrder()))
                .map(Mapper::toBanner)
                .toList();
    }

    @Transactional
    public BannerResponse create(BannerRequest request) {
        Banner banner = new Banner();
        Mapper.mergeBanner(banner, request);
        return Mapper.toBanner(bannerRepository.save(banner));
    }

    @Transactional
    public BannerResponse update(Long id, BannerRequest request) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy banner"));
        Mapper.mergeBanner(banner, request);
        return Mapper.toBanner(bannerRepository.save(banner));
    }

    @Transactional
    public void delete(Long id) {
        if (!bannerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy banner");
        }
        bannerRepository.deleteById(id);
    }
}
