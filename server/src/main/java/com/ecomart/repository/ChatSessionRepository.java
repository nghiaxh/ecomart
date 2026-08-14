package com.ecomart.repository;

import com.ecomart.domain.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    List<ChatSession> findByUserIdOrderByCreatedAtDesc(Long userId);
}
