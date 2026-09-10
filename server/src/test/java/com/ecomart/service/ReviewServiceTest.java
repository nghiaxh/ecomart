package com.ecomart.service;

import com.ecomart.common.SecurityUtils;
import com.ecomart.domain.entity.Customer;
import com.ecomart.domain.entity.Product;
import com.ecomart.domain.entity.Review;
import com.ecomart.dto.request.ReviewRequest;
import com.ecomart.dto.response.ReviewResponse;
import com.ecomart.exception.BadRequestException;
import com.ecomart.repository.ProductRepository;
import com.ecomart.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock SecurityUtils securityUtils;
    @Mock ReviewRepository reviewRepository;
    @Mock ProductRepository productRepository;

    private ReviewService service;

    @BeforeEach
    void setUp() {
        service = new ReviewService(securityUtils, reviewRepository, productRepository);
    }

    private Customer customer(long id) {
        Customer c = new Customer();
        c.setId(id);
        c.setUsername("minh");
        return c;
    }

    private Product product(long id) {
        Product p = new Product();
        p.setId(id);
        return p;
    }

    private Review review(long id, boolean hidden) {
        Review r = new Review();
        r.setId(id);
        r.setCustomer(customer(1L));
        r.setRating(5);
        r.setContent("Tot");
        r.setHidden(hidden);
        return r;
    }

    @Test
    void createRejectsDuplicateReviewForSameProduct() {
        when(securityUtils.currentUser()).thenReturn(customer(1L));
        when(productRepository.findById(5L)).thenReturn(Optional.of(product(5L)));
        when(reviewRepository.existsByCustomerIdAndProductId(1L, 5L)).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.create(new ReviewRequest(5L, 5, "Co tot")));

        assertEquals("You have already reviewed this product", ex.getMessage());
    }

    @Test
    void publicListFiltersHiddenReviews() {
        when(reviewRepository.findByProductIdAndIsHiddenFalseOrderByCreatedAtDesc(5L))
                .thenReturn(List.of(review(1L, false)));

        List<ReviewResponse> result = service.listForProduct(5L, false);

        assertEquals(1, result.size());
        assertEquals("minh", result.get(0).customerName());
    }

    @Test
    void nonAdminCannotIncludeHiddenReviews() {
        when(securityUtils.currentUserHasRole("ADMIN")).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> service.listForProduct(5L, true));
    }

    @Test
    void adminIncludeHiddenListsAllReviews() {
        when(securityUtils.currentUserHasRole("ADMIN")).thenReturn(true);
        when(reviewRepository.findByProductIdOrderByCreatedAtDesc(5L))
                .thenReturn(List.of(review(1L, true), review(2L, false)));

        List<ReviewResponse> result = service.listForProduct(5L, true);

        assertEquals(2, result.size());
    }

    @Test
    void toggleHiddenFlipsFlag() {
        Review review = review(1L, false);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        ReviewResponse response = service.toggleHidden(1L);

        assertTrue(response.hidden());
        verify(reviewRepository).save(review);
    }
}