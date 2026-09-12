package com.ecomart.service;

import com.ecomart.common.Mapper;
import com.ecomart.common.SecurityUtils;
import com.ecomart.domain.entity.Customer;
import com.ecomart.domain.entity.Product;
import com.ecomart.domain.entity.Review;
import com.ecomart.dto.request.ReviewRequest;
import com.ecomart.dto.response.ReviewResponse;
import com.ecomart.exception.BadRequestException;
import com.ecomart.exception.ResourceNotFoundException;
import com.ecomart.repository.ProductRepository;
import com.ecomart.repository.ReviewRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {

    private final SecurityUtils securityUtils;
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final ActivityLogService activityLogService;

    public ReviewService(SecurityUtils securityUtils,
                         ReviewRepository reviewRepository,
                         ProductRepository productRepository,
                         ActivityLogService activityLogService) {
        this.securityUtils = securityUtils;
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.activityLogService = activityLogService;
    }

    @Transactional
    public ReviewResponse create(ReviewRequest request) {
        Customer customer = (Customer) securityUtils.currentUser();
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        if (reviewRepository.existsByCustomerIdAndProductId(customer.getId(), product.getId())) {
            throw new BadRequestException("You have already reviewed this product");
        }
        Review review = new Review();
        review.setCustomer(customer);
        review.setProduct(product);
        review.setRating(request.rating());
        review.setContent(request.content());
        review.setHidden(false);
        return Mapper.toReview(reviewRepository.save(review));
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> listForProduct(Long productId, boolean includeHidden) {
        if (includeHidden && !securityUtils.currentUserHasAnyRole("ADMIN", "STAFF")) {
            throw new AccessDeniedException("You do not have permission to view hidden reviews");
        }
        List<Review> reviews = includeHidden
                ? reviewRepository.findByProductIdOrderByCreatedAtDesc(productId)
                : reviewRepository.findByProductIdAndIsHiddenFalseOrderByCreatedAtDesc(productId);
        return reviews.stream().map(Mapper::toReview).toList();
    }

    @Transactional
    public ReviewResponse toggleHidden(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        review.setHidden(!review.isHidden());
        ReviewResponse saved = Mapper.toReview(reviewRepository.save(review));
        activityLogService.record(ActivityLogService.TOGGLE_REVIEW_VISIBILITY, ActivityLogService.TYPE_REVIEW,
                saved.id(), saved.customerName(), (saved.hidden() ? "Ẩn" : "Hiện") + " đánh giá của " + saved.customerName());
        return saved;
    }
}
