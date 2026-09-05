package com.ecomart.controller;

import com.ecomart.common.SecurityUtils;
import com.ecomart.dto.request.ProductRequest;
import com.ecomart.dto.response.MessageResponse;
import com.ecomart.dto.response.PageResponse;
import com.ecomart.dto.response.ProductResponse;
import com.ecomart.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final SecurityUtils securityUtils;

    public ProductController(ProductService productService, SecurityUtils securityUtils) {
        this.productService = productService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public PageResponse<ProductResponse> search(@RequestParam(required = false) String q,
                                                 @RequestParam(required = false) Long category,
                                                 @RequestParam(required = false) Double minPrice,
                                                 @RequestParam(required = false) Double maxPrice,
                                                 @RequestParam(defaultValue = "false") boolean showAll,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "12") int size) {
        boolean onlyActive = !showAll || !securityUtils.currentUserHasRole("ADMIN");
        return productService.search(q, category, minPrice, maxPrice, onlyActive, PageRequest.of(page, size));
    }

    @GetMapping("/latest")
    public List<ProductResponse> latest() {
        return productService.latest();
    }

    @GetMapping("/slug/{slug}")
    public ProductResponse getBySlug(@PathVariable String slug) {
        return productService.getBySlug(slug);
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable Long id) {
        return productService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse toggleActive(@PathVariable Long id) {
        productService.toggleActive(id);
        return productService.getById(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public MessageResponse delete(@PathVariable Long id) {
        productService.delete(id);
        return new MessageResponse("Đã xóa sản phẩm");
    }
}
