package com.ecomart.service;

import com.ecomart.common.Mapper;
import com.ecomart.domain.entity.Customer;
import com.ecomart.domain.entity.Product;
import com.ecomart.domain.entity.Review;
import com.ecomart.dto.request.ReviewRequest;
import com.ecomart.dto.response.ReviewResponse;
import com.ecomart.exception.BadRequestException;
import com.ecomart.exception.ResourceNotFoundException;
import com.ecomart.repository.ProductRepository;
import com.ecomart.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {

    private final com.ecomart.common.SecurityUtils securityUtils;
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    public ReviewService(com.ecomart.common.SecurityUtils securityUtils,
                         ReviewRepository reviewRepository,
                         ProductRepository productRepository) {
        this.securityUtils = securityUtils;
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public ReviewResponse create(ReviewRequest request) {
        Customer customer = (Customer) securityUtils.currentUser();
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));
        if (reviewRepository.existsByCustomerIdAndProductId(customer.getId(), product.getId())) {
            throw new BadRequestException("Bạn đã đánh giá sản phẩm này");
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
        List<Review> reviews = includeHidden
                ? reviewRepository.findByProductIdOrderByCreatedAtDesc(productId)
                : reviewRepository.findByProductIdAndIsHiddenFalseOrderByCreatedAtDesc(productId);
        return reviews.stream().map(Mapper::toReview).toList();
    }

    @Transactional
    public void toggleHidden(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá"));
        review.setHidden(!review.isHidden());
        reviewRepository.save(review);
    }
}
