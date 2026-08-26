package com.ecomart.controller;

import com.ecomart.dto.request.ReviewRequest;
import com.ecomart.dto.response.ReviewResponse;
import com.ecomart.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public List<ReviewResponse> listForProduct(@RequestParam Long productId,
                                               @RequestParam(defaultValue = "false") boolean includeHidden) {
        return reviewService.listForProduct(productId, includeHidden);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse create(@Valid @RequestBody ReviewRequest request) {
        return reviewService.create(request);
    }

    @PatchMapping("/{id}/toggle")
    public void toggleHidden(@PathVariable Long id) {
        reviewService.toggleHidden(id);
    }
}
