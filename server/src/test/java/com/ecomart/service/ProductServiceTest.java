package com.ecomart.service;

import com.ecomart.domain.entity.Category;
import com.ecomart.domain.entity.Material;
import com.ecomart.domain.entity.Product;
import com.ecomart.dto.request.ProductRequest;
import com.ecomart.dto.response.PageResponse;
import com.ecomart.dto.response.ProductResponse;
import com.ecomart.exception.BadRequestException;
import com.ecomart.exception.ResourceNotFoundException;
import com.ecomart.repository.CategoryRepository;
import com.ecomart.repository.MaterialRepository;
import com.ecomart.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock ProductRepository productRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock MaterialRepository materialRepository;

    private ProductService service;

    @BeforeEach
    void setUp() {
        service = new ProductService(productRepository, categoryRepository, materialRepository,
                mock(ActivityLogService.class));
    }

    private Category category(long id) {
        Category c = new Category();
        c.setId(id);
        c.setName("Danh muc " + id);
        c.setSlug("slug-" + id);
        return c;
    }

    private Product product(long id, Category category) {
        Product p = new Product();
        p.setId(id);
        p.setName("San pham " + id);
        p.setSlug("sp-" + id);
        p.setPrice(10000);
        p.setStock(10);
        p.setActive(true);
        p.setCategory(category);
        return p;
    }

    private ProductRequest requestWithMaterials(Long categoryId) {
        return new ProductRequest("Bơ", "bo", "mo ta", 25000.0, 10, 0.5, "Việt Nam", categoryId, true,
                List.of(), List.of(new ProductRequest.ProductMaterialRequest(10L, 100.0)));
    }

    @Test
    void searchParentCategoryIncludesDescendants() {
        Category parent = category(1L);
        Category childA = category(2L);
        Category childB = category(3L);
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(categoryRepository.findByParentId(1L)).thenReturn(List.of(childA, childB));
        when(categoryRepository.findByParentId(2L)).thenReturn(List.of());
        when(categoryRepository.findByParentId(3L)).thenReturn(List.of());
        when(productRepository.search(eq(""), any(), eq(null), eq(null), eq(false), any()))
                .thenReturn(new PageImpl<>(List.of(product(10L, childA), product(11L, childB))));

        PageResponse<ProductResponse> result =
                service.search(null, 1L, null, null, true, true, PageRequest.of(0, 12));

        assertEquals(2, result.content().size());
        ArgumentCaptor<List<Long>> ids = ArgumentCaptor.forClass(List.class);
        verify(productRepository).search(eq(""), ids.capture(), eq(null), eq(null), eq(false), any());
        assertTrue(ids.getValue().containsAll(List.of(1L, 2L, 3L)));
    }

    @Test
    void searchLeafCategoryPassesSingleId() {
        when(categoryRepository.existsById(2L)).thenReturn(true);
        when(categoryRepository.findByParentId(2L)).thenReturn(List.of());
        when(productRepository.search(eq(""), any(), eq(null), eq(null), eq(false), any()))
                .thenReturn(new PageImpl<>(List.of(product(10L, category(2L)))));

        PageResponse<ProductResponse> result =
                service.search(null, 2L, null, null, true, true, PageRequest.of(0, 12));

        assertEquals(1, result.content().size());
        ArgumentCaptor<List<Long>> ids = ArgumentCaptor.forClass(List.class);
        verify(productRepository).search(eq(""), ids.capture(), eq(null), eq(null), eq(false), any());
        assertEquals(List.of(2L), ids.getValue());
    }

    @Test
    void searchUnknownCategoryReturnsEmptyWithoutQueryingProducts() {
        when(categoryRepository.existsById(999L)).thenReturn(false);

        PageResponse<ProductResponse> result =
                service.search(null, 999L, null, null, true, true, PageRequest.of(0, 12));

        assertTrue(result.content().isEmpty());
        assertEquals(0, result.totalElements());
        verify(productRepository, never()).search(any(), any(), any(), any(), eq(false), any());
    }

    @Test
    void searchWithoutCategoryPassesNull() {
        when(productRepository.search(eq(""), any(), eq(null), eq(null), eq(false), any()))
                .thenReturn(new PageImpl<>(List.of(product(10L, category(2L)))));

        service.search(null, null, null, null, true, true, PageRequest.of(0, 12));

        ArgumentCaptor<List<Long>> ids = ArgumentCaptor.forClass(List.class);
        verify(productRepository).search(eq(""), ids.capture(), eq(null), eq(null), eq(false), any());
        assertNull(ids.getValue());
    }

    @Test
    void createRejectsDuplicateSlug() {
        when(productRepository.existsBySlug("bo")).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.create(requestWithMaterials(1L)));

        assertEquals("Slug already exists", ex.getMessage());
    }

    @Test
    void createWithMaterialsAttachesMaterialAndReturnsResponse() {
        Category category = category(1L);
        Material material = new Material();
        material.setId(10L);
        material.setName("Hữu cơ");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(materialRepository.findById(10L)).thenReturn(Optional.of(material));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductResponse response = service.create(requestWithMaterials(1L));

        assertEquals(1, response.materials().size());
        assertEquals("Hữu cơ", response.materials().get(0).name());
        assertEquals(25000.0, response.price());
        assertEquals("bo", response.slug());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void getBySlugThrowsWhenProductMissing() {
        when(productRepository.findBySlug("khong-ton-tai")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getBySlug("khong-ton-tai"));
    }
}