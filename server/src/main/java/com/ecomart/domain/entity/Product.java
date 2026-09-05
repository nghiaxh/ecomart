package com.ecomart.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_products_category", columnList = "category_id"),
        @Index(name = "idx_products_slug", columnList = "slug")
})
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(length = 4000)
    private String description;

    @Column(nullable = false)
    private double price;

    @Column(nullable = false)
    private int stock;

    private double weight;

    private String origin;

    @Column(nullable = false)
    private boolean isActive = true;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductMaterial> materials = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.Instant createdAt = java.time.Instant.now();

    @Version
    private long version;
}
