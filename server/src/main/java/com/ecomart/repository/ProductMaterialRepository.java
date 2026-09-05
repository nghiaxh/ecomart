package com.ecomart.repository;

import com.ecomart.domain.entity.ProductMaterial;
import com.ecomart.domain.entity.ProductMaterialId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductMaterialRepository extends JpaRepository<ProductMaterial, ProductMaterialId> {
}
