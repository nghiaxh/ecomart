package com.ecomart.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "banners")
@Getter
@Setter
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String subtitle;

    @Column(nullable = false)
    private String imageUrl;

    private String linkUrl;

    @Column(nullable = false)
    private int displayOrder = 0;

    @Column(nullable = false)
    private boolean isActive = true;
}
