package com.ecomart.service;

import com.ecomart.common.Mapper;
import com.ecomart.domain.entity.Category;
import com.ecomart.dto.request.CategoryRequest;
import com.ecomart.dto.response.CategoryResponse;
import com.ecomart.exception.BadRequestException;
import com.ecomart.exception.ResourceNotFoundException;
import com.ecomart.repository.CategoryRepository;
import com.ecomart.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ActivityLogService activityLogService;

    public CategoryService(CategoryRepository categoryRepository,
                           ProductRepository productRepository,
                           ActivityLogService activityLogService) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.activityLogService = activityLogService;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> tree() {
        return categoryRepository.findByParentIsNullOrderByDisplayOrderAsc().stream()
                .map(Mapper::toCategory)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        return Mapper.toCategory(category);
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String slug = resolveSlug(request);
        if (categoryRepository.existsBySlug(slug)) {
            throw new BadRequestException("Category slug already exists");
        }
        Category category = new Category();
        Mapper.mergeCategory(category, request, resolveParent(request), slug);
        CategoryResponse saved = Mapper.toCategory(categoryRepository.save(category));
        activityLogService.record(ActivityLogService.CREATE_CATEGORY, ActivityLogService.TYPE_CATEGORY,
                saved.id(), saved.name(), "Tạo danh mục " + saved.name());
        return saved;
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        String slug = request.slug() == null || request.slug().isBlank()
                ? category.getSlug()
                : resolveSlug(request);
        if (!slug.equals(category.getSlug()) && categoryRepository.existsBySlug(slug)) {
            throw new BadRequestException("Category slug already exists");
        }
        Mapper.mergeCategory(category, request, resolveParent(request), slug);
        CategoryResponse saved = Mapper.toCategory(categoryRepository.save(category));
        activityLogService.record(ActivityLogService.UPDATE_CATEGORY, ActivityLogService.TYPE_CATEGORY,
                saved.id(), saved.name(), "Cập nhật danh mục " + saved.name());
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        if (!categoryRepository.findByParentId(id).isEmpty()) {
            throw new BadRequestException("Cannot delete a category that has child categories");
        }
        if (productRepository.existsByCategoryId(id)) {
            throw new BadRequestException("Cannot delete a category that contains products");
        }
        categoryRepository.deleteById(id);
        activityLogService.record(ActivityLogService.DELETE_CATEGORY, ActivityLogService.TYPE_CATEGORY,
                id, category.getName(), "Xóa danh mục " + category.getName());
    }

    private Category resolveParent(CategoryRequest req) {
        if (req.parentId() == null) {
            return null;
        }
        return categoryRepository.findById(req.parentId())
                .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
    }

    private String resolveSlug(CategoryRequest req) {
        if (req.slug() != null && !req.slug().isBlank()) {
            return req.slug().trim().toLowerCase(Locale.ROOT);
        }
        String raw = Normalizer.normalize(req.name().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replace('\u0111', 'd')
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return raw.isBlank() ? "danh-muc" : raw;
    }
}
