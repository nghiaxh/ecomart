package com.ecomart.repository;

import com.ecomart.domain.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Override
    @EntityGraph(attributePaths = {"images", "category"})
    Optional<Product> findById(Long id);

    @EntityGraph(attributePaths = {"images", "category"})
    Optional<Product> findBySlug(String slug);

    boolean existsBySlug(String slug);
    boolean existsByCategoryId(Long categoryId);

    @EntityGraph(attributePaths = {"images", "category"})
    List<Product> findTop8ByIsActiveTrueOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"category"})
    List<Product> findByIsActiveTrueAndStockOrderByNameAsc(int stock);

    @Query("SELECT p FROM Product p WHERE (:onlyActive = false OR p.isActive = true) "
            + "AND (:keyword = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) "
            + "AND (:categoryIds IS NULL OR p.category.id IN :categoryIds) "
            + "AND (:minPrice IS NULL OR p.price >= :minPrice) "
            + "AND (:maxPrice IS NULL OR p.price <= :maxPrice)")
    @EntityGraph(attributePaths = {"images", "category"})
    Page<Product> search(@Param("keyword") String keyword,
                         @Param("categoryIds") List<Long> categoryIds,
                         @Param("minPrice") Double minPrice,
                         @Param("maxPrice") Double maxPrice,
                         @Param("onlyActive") boolean onlyActive,
                         Pageable pageable);
}
