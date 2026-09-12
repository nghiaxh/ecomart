package com.ecomart.service;

import com.ecomart.domain.entity.Category;
import com.ecomart.dto.request.CategoryRequest;
import com.ecomart.dto.response.CategoryResponse;
import com.ecomart.exception.BadRequestException;
import com.ecomart.exception.ResourceNotFoundException;
import com.ecomart.repository.CategoryRepository;
import com.ecomart.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock CategoryRepository categoryRepository;
    @Mock ProductRepository productRepository;

    private CategoryService service;

    @BeforeEach
    void setUp() {
        service = new CategoryService(categoryRepository, productRepository, mock(ActivityLogService.class));
    }

    private CategoryRequest request(String name, String slug) {
        return new CategoryRequest(null, name, slug, "leaf", 1, true);
    }

    private Category category(long id) {
        Category category = new Category();
        category.setId(id);
        category.setName("Rau củ quả");
        return category;
    }

    @Test
    void createAutoGeneratesSlugFromVietnameseName() {
        when(categoryRepository.existsBySlug("rau-cu-qua")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        CategoryResponse response = service.create(request("Rau củ quả", null));

        assertEquals("rau-cu-qua", response.slug());
        verify(categoryRepository, never()).findById(any());
    }

    @Test
    void createRejectsDuplicateGeneratedSlug() {
        when(categoryRepository.existsBySlug("rau-cu-qua")).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.create(request("Rau củ quả", null)));

        assertEquals("Category slug already exists", ex.getMessage());
    }

    @Test
    void deleteCategoryWithProductsThrows() {
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category(5L)));
        when(categoryRepository.findByParentId(5L)).thenReturn(List.of());
        when(productRepository.existsByCategoryId(5L)).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.delete(5L));

        assertEquals("Cannot delete a category that contains products", ex.getMessage());
        verify(categoryRepository, never()).deleteById(5L);
    }

    @Test
    void deleteMissingCategoryThrows() {
        when(categoryRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.delete(404L));
    }

    @Test
    void selectCategoryWithMissingParentThrows() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.create(new CategoryRequest(99L, "Con", "con", "leaf", 2, true)));
    }
}