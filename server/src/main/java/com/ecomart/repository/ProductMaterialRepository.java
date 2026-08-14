package com.ecomart.repository;

import com.ecomart.domain.entity.ProductMaterial;
import com.ecomart.domain.entity.ProductMaterialId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductMaterialRepository extends JpaRepository<ProductMaterial, ProductMaterialId> {
    List<ProductMaterial> findByProductId(Long productId);
}
