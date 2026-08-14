package com.ecomart.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "product_materials")
@Getter
@Setter
public class ProductMaterial {

    @EmbeddedId
    private ProductMaterialId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId")
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("materialId")
    @JoinColumn(name = "material_id")
    private Material material;

    @Column(nullable = false)
    private double percentage;
}
