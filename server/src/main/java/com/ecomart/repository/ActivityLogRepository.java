package com.ecomart.repository;

import com.ecomart.domain.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    Page<ActivityLog> findByUsernameContainingIgnoreCaseOrActionContainingIgnoreCaseOrDetailContainingIgnoreCase(
            String username, String action, String detail, Pageable pageable);
}