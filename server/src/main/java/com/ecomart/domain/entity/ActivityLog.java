package com.ecomart.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "activity_logs", indexes = {
        @Index(name = "idx_activity_logs_created_at", columnList = "createdAt"),
        @Index(name = "idx_activity_logs_user_id", columnList = "userId")
})
@Getter
@Setter
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(length = 50)
    private String username;

    @Column(length = 20)
    private String role;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(length = 30)
    private String entityType;

    private Long entityId;

    @Column(length = 200)
    private String entityName;

    @Column(length = 500)
    private String detail;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}