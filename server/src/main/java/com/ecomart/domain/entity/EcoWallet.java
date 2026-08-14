package com.ecomart.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "eco_wallets")
@Getter
@Setter
public class EcoWallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", unique = true, nullable = false)
    private Customer customer;

    @Column(nullable = false)
    private int balance = 0;

    @Column(nullable = false)
    private int totalEarned = 0;
}
