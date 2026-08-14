package com.ecomart.repository;

import com.ecomart.domain.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Long> {
    List<Banner> findByIsActiveTrueOrderByDisplayOrderAsc();
}
