package com.ecomart.service;

import com.ecomart.domain.entity.Category;
import com.ecomart.domain.entity.Product;
import com.ecomart.dto.response.PageResponse;
import com.ecomart.dto.response.ProductResponse;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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
        service = new ProductService(productRepository, categoryRepository, materialRepository);
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

    @Test
    void searchParentCategoryIncludesDescendants() {
        Category parent = category(1L);
        Category childA = category(2L);
        Category childB = category(3L);
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(categoryRepository.findByParentId(1L)).thenReturn(List.of(childA, childB));
        when(categoryRepository.findByParentId(2L)).thenReturn(List.of());
        when(categoryRepository.findByParentId(3L)).thenReturn(List.of());
        when(productRepository.search(eq(""), any(), eq(null), eq(null), eq(true), any()))
                .thenReturn(new PageImpl<>(List.of(product(10L, childA), product(11L, childB))));

        PageResponse<ProductResponse> result =
                service.search(null, 1L, null, null, true, true, PageRequest.of(0, 12));

        assertEquals(2, result.content().size());
        ArgumentCaptor<List<Long>> ids = ArgumentCaptor.forClass(List.class);
        verify(productRepository).search(eq(""), ids.capture(), eq(null), eq(null), eq(true), any());
        assertTrue(ids.getValue().containsAll(List.of(1L, 2L, 3L)));
    }

    @Test
    void searchLeafCategoryPassesSingleId() {
        when(categoryRepository.existsById(2L)).thenReturn(true);
        when(categoryRepository.findByParentId(2L)).thenReturn(List.of());
        when(productRepository.search(eq(""), any(), eq(null), eq(null), eq(true), any()))
                .thenReturn(new PageImpl<>(List.of(product(10L, category(2L)))));

        PageResponse<ProductResponse> result =
                service.search(null, 2L, null, null, true, true, PageRequest.of(0, 12));

        assertEquals(1, result.content().size());
        ArgumentCaptor<List<Long>> ids = ArgumentCaptor.forClass(List.class);
        verify(productRepository).search(eq(""), ids.capture(), eq(null), eq(null), eq(true), any());
        assertEquals(List.of(2L), ids.getValue());
    }

    @Test
    void searchUnknownCategoryReturnsEmptyWithoutQueryingProducts() {
        when(categoryRepository.existsById(999L)).thenReturn(false);

        PageResponse<ProductResponse> result =
                service.search(null, 999L, null, null, true, true, PageRequest.of(0, 12));

        assertTrue(result.content().isEmpty());
        assertEquals(0, result.totalElements());
        verify(productRepository, never()).search(any(), any(), any(), any(), eq(true), any());
    }

    @Test
    void searchWithoutCategoryPassesNull() {
        when(productRepository.search(eq(""), any(), eq(null), eq(null), eq(true), any()))
                .thenReturn(new PageImpl<>(List.of(product(10L, category(2L)))));

        service.search(null, null, null, null, true, true, PageRequest.of(0, 12));

        ArgumentCaptor<List<Long>> ids = ArgumentCaptor.forClass(List.class);
        verify(productRepository).search(eq(""), ids.capture(), eq(null), eq(null), eq(true), any());
        assertNull(ids.getValue());
    }
}
