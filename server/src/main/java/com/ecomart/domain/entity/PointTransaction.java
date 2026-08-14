package com.ecomart.domain.entity;

import com.ecomart.domain.enums.PointTransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "point_transactions")
@Getter
@Setter
public class PointTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private EcoWallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PointTransactionType type;

    @Column(nullable = false)
    private int amount;

    private String description;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
