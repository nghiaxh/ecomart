package com.ecomart.service;

import com.ecomart.common.Mapper;
import com.ecomart.domain.entity.Category;
import com.ecomart.dto.request.CategoryRequest;
import com.ecomart.dto.response.CategoryResponse;
import com.ecomart.exception.BadRequestException;
import com.ecomart.exception.ResourceNotFoundException;
import com.ecomart.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
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
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục"));
        return Mapper.toCategory(category);
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsBySlug(request.slug)) {
            throw new BadRequestException("Slug danh mục đã tồn tại");
        }
        Category category = new Category();
        apply(category, request);
        return Mapper.toCategory(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục"));
        apply(category, request);
        return Mapper.toCategory(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy danh mục");
        }
        categoryRepository.deleteById(id);
    }

    private void apply(Category category, CategoryRequest req) {
        if (req.parentId != null) {
            Category parent = categoryRepository.findById(req.parentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục cha"));
            category.setParent(parent);
        }
        category.setName(req.name);
        category.setSlug(req.slug);
        category.setIcon(req.icon);
        category.setDisplayOrder(req.displayOrder == null ? 0 : req.displayOrder);
        category.setActive(req.active);
    }
}
