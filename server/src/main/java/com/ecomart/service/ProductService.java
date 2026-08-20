package com.ecomart.service;

import com.ecomart.common.Mapper;
import com.ecomart.domain.entity.Category;
import com.ecomart.domain.entity.Product;
import com.ecomart.domain.entity.ProductImage;
import com.ecomart.domain.entity.ProductMaterial;
import com.ecomart.domain.entity.ProductMaterialId;
import com.ecomart.domain.entity.Material;
import com.ecomart.dto.request.ProductRequest;
import com.ecomart.dto.response.PageResponse;
import com.ecomart.dto.response.ProductResponse;
import com.ecomart.exception.BadRequestException;
import com.ecomart.exception.ResourceNotFoundException;
import com.ecomart.repository.CategoryRepository;
import com.ecomart.repository.MaterialRepository;
import com.ecomart.repository.ProductRepository;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final MaterialRepository materialRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          MaterialRepository materialRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.materialRepository = materialRepository;
    }

    public PageResponse<ProductResponse> search(String keyword, Long categoryId, Double minPrice,
                                                Double maxPrice, boolean onlyActive, Pageable pageable) {
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (onlyActive) {
                predicates.add(cb.isTrue(root.get("isActive")));
            }
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("description")), like)));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Product> page = productRepository.findAll(spec, pageable);
        List<ProductResponse> content = page.getContent().stream().map(Mapper::toProduct).toList();
        return Mapper.toPage(page, content);
    }

    public ProductResponse getBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));
        return Mapper.toProduct(product);
    }

    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));
        return Mapper.toProduct(product);
    }

    public List<ProductResponse> latest() {
        return productRepository.findTop8ByIsActiveTrueOrderByCreatedAtDesc().stream()
                .map(Mapper::toProduct)
                .toList();
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsBySlug(request.slug)) {
            throw new BadRequestException("Slug đã tồn tại");
        }
        Category category = categoryRepository.findById(request.categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục"));
        Product product = apply(productRepository.save(new Product()), request, category);
        return Mapper.toProduct(product);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));
        Category category = categoryRepository.findById(request.categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục"));
        return Mapper.toProduct(apply(product, request, category));
    }

    @Transactional
    public void toggleActive(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));
        product.setActive(!product.isActive());
        productRepository.save(product);
    }

    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy sản phẩm");
        }
        productRepository.deleteById(id);
    }

    private Product apply(Product product, ProductRequest req, Category category) {
        product.setName(req.name);
        product.setSlug(req.slug);
        product.setDescription(req.description);
        product.setPrice(req.price);
        product.setStock(req.stock);
        product.setCarbonIndex(req.carbonIndex == null ? 0 : req.carbonIndex);
        product.setBaselineCarbonIndex(req.baselineCarbonIndex == null ? 0 : req.baselineCarbonIndex);
        product.setEcoPointsPerUnit(req.ecoPointsPerUnit == null ? 0 : req.ecoPointsPerUnit);
        product.setWeight(req.weight == null ? 0 : req.weight);
        product.setOrigin(req.origin);
        product.setCategory(category);
        product.setActive(req.active);

        product.getImages().clear();
        if (req.images != null) {
            int order = 0;
            for (ProductRequest.ProductImageRequest img : req.images) {
                ProductImage pi = new ProductImage();
                pi.setProduct(product);
                pi.setUrl(img.url());
                pi.setPrimary(img.primary());
                pi.setDisplayOrder(img.displayOrder() == null ? order : img.displayOrder());
                product.getImages().add(pi);
                order++;
            }
        }

        product.getMaterials().clear();
        if (req.materials != null) {
            for (ProductRequest.ProductMaterialRequest m : req.materials) {
                Material material = materialRepository.findById(m.materialId())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vật liệu"));
                ProductMaterial pm = new ProductMaterial();
                pm.setId(new ProductMaterialId(product.getId(), material.getId()));
                pm.setProduct(product);
                pm.setMaterial(material);
                pm.setPercentage(m.percentage());
                product.getMaterials().add(pm);
            }
        }
        return productRepository.save(product);
    }
}
