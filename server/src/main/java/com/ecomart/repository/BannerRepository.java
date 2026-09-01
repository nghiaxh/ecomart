package com.ecomart.repository;

import com.ecomart.domain.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BannerRepository extends JpaRepository<Banner, Long> {
    List<Banner> findByIsActiveTrueOrderByDisplayOrderAsc();
    Optional<Banner> findByTitle(String title);
}
