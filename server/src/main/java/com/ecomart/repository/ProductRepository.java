package com.ecomart.repository;

import com.ecomart.domain.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    @Override
    @EntityGraph(attributePaths = {"images", "materials", "materials.material", "category"})
    Optional<Product> findById(Long id);

    Optional<Product> findBySlug(String slug);

    boolean existsBySlug(String slug);
    boolean existsByCategoryId(Long categoryId);

    List<Product> findTop8ByIsActiveTrueOrderByCreatedAtDesc();

    @Query("SELECT p FROM Product p LEFT JOIN p.category c WHERE p.isActive = true "
            + "AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Product> searchActiveByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE (:onlyActive = false OR p.isActive = true) "
            + "AND (:keyword = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) "
            + "AND (:categoryId IS NULL OR p.category.id = :categoryId) "
            + "AND (:minPrice IS NULL OR p.price >= :minPrice) "
            + "AND (:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> search(@Param("keyword") String keyword,
                         @Param("categoryId") Long categoryId,
                         @Param("minPrice") Double minPrice,
                         @Param("maxPrice") Double maxPrice,
                         @Param("onlyActive") boolean onlyActive,
                         Pageable pageable);
}
