package com.ecomart.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "customers")
@DiscriminatorValue("CUSTOMER")
@Getter
@Setter
public class Customer extends User {

    @Column(nullable = false)
    private int ecoPoints = 0;

    private double totalCo2Saved = 0;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;
}
