package com.ecomart.service;

import com.ecomart.common.Mapper;
import com.ecomart.domain.entity.Category;
import com.ecomart.domain.entity.Product;
import com.ecomart.domain.entity.Material;
import com.ecomart.dto.request.ProductRequest;
import com.ecomart.dto.response.PageResponse;
import com.ecomart.dto.response.ProductResponse;
import com.ecomart.exception.BadRequestException;
import com.ecomart.exception.ResourceNotFoundException;
import com.ecomart.repository.CategoryRepository;
import com.ecomart.repository.MaterialRepository;
import com.ecomart.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
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

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(String keyword, Long categoryId, Double minPrice,
                                                Double maxPrice, boolean showAll, boolean isAdmin, Pageable pageable) {
        return search(keyword, categoryId, minPrice, maxPrice, onlyActive(showAll, isAdmin), pageable);
    }

    private PageResponse<ProductResponse> search(String keyword, Long categoryId, Double minPrice,
                                                Double maxPrice, boolean onlyActive, Pageable pageable) {
        String q = keyword == null || keyword.isBlank() ? "" : keyword.trim();
        List<Long> categoryIds = resolveCategoryIds(categoryId);
        if (categoryId != null && categoryIds.isEmpty()) {
            return Mapper.toPage(Page.<Product>empty(pageable), List.<ProductResponse>of());
        }
        Page<Product> page = productRepository.search(q, categoryIds, minPrice, maxPrice, onlyActive, pageable);
        List<ProductResponse> content = page.getContent().stream().map(Mapper::toProduct).toList();
        return Mapper.toPage(page, content);
    }

    private List<Long> resolveCategoryIds(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        if (!categoryRepository.existsById(categoryId)) {
            return List.of();
        }
        List<Long> ids = new ArrayList<>();
        Deque<Long> queue = new ArrayDeque<>();
        queue.add(categoryId);
        while (!queue.isEmpty()) {
            Long current = queue.poll();
            ids.add(current);
            for (Category child : categoryRepository.findByParentId(current)) {
                queue.add(child.getId());
            }
        }
        return ids;
    }

    private boolean onlyActive(boolean showAll, boolean isAdmin) {
        return !showAll || !isAdmin;
    }

    @Transactional(readOnly = true)
    public ProductResponse getBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return Mapper.toProduct(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return Mapper.toProduct(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> latest() {
        return productRepository.findTop8ByIsActiveTrueOrderByCreatedAtDesc().stream()
                .map(Mapper::toProduct)
                .toList();
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsBySlug(request.slug())) {
            throw new BadRequestException("Slug already exists");
        }
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        Product product = new Product();
        Mapper.mergeProduct(product, request, category);
        attachMaterials(product, request);
        return Mapper.toProduct(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        Mapper.mergeProduct(product, request, category);
        product.getMaterials().clear();
        attachMaterials(product, request);
        return Mapper.toProduct(productRepository.save(product));
    }

    @Transactional
    public void toggleActive(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        product.setActive(!product.isActive());
        productRepository.save(product);
    }

    @Transactional
    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        product.setActive(false);
        productRepository.save(product);
    }

    private void attachMaterials(Product product, ProductRequest req) {
        if (req.materials() == null) {
            return;
        }
        for (ProductRequest.ProductMaterialRequest m : req.materials()) {
            Material material = materialRepository.findById(m.materialId())
                    .orElseThrow(() -> new ResourceNotFoundException("Material not found"));
            product.getMaterials().add(Mapper.productMaterial(product, material, m.percentage()));
        }
    }
}
