package com.ecomart.repository;

import com.ecomart.domain.entity.User;
import com.ecomart.domain.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    long countByRole(UserRole role);
}
