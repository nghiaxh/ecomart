package com.ecomart.common;

import com.ecomart.dto.request.*;
import com.ecomart.dto.response.*;
import com.ecomart.domain.entity.*;

import java.util.List;

public final class Mapper {

    private Mapper() {
    }

    public static AuthResponse toAuth(User user, String accessToken, String refreshToken, long expiresIn) {
        return new AuthResponse(accessToken, refreshToken, expiresIn, user.getId(), user.getUsername(),
                user.getEmail(), user.getNumberPhone(), user.getAvatarUrl(), user.getRole());
    }

    public static ProfileResponse toProfile(User user) {
        return new ProfileResponse(user.getId(), user.getUsername(), user.getEmail(), user.getNumberPhone(),
                user.getAvatarUrl(), user.getRole(), user.getCreatedAt());
    }

    public static ProductResponse toProduct(Product p) {
        List<String> images = p.getImages().stream()
                .sorted((a, b) -> Integer.compare(a.getDisplayOrder(), b.getDisplayOrder()))
                .map(ProductImage::getUrl)
                .toList();

        List<ProductResponse.MaterialInfo> materials = p.getMaterials().stream()
                .map(pm -> new ProductResponse.MaterialInfo(
                        pm.getMaterial().getId(),
                        pm.getMaterial().getName(),
                        pm.getPercentage(),
                        pm.getMaterial().getType() == null ? null : pm.getMaterial().getType().name()))
                .toList();

        return new ProductResponse(p.getId(), p.getName(), p.getSlug(), p.getDescription(), p.getPrice(),
                p.getStock(), p.getWeight(), p.getOrigin(),
                p.getCategory() == null ? null : p.getCategory().getId(),
                p.getCategory() == null ? null : p.getCategory().getName(),
                p.getCategory() == null ? null : p.getCategory().getSlug(),
                p.isActive(), images, materials);
    }

    public static CategoryResponse toCategory(Category c) {
        CategoryResponse resp = CategoryResponse.ofRoot(c.getId(), c.getName(), c.getSlug(), c.getIcon(),
                c.getDisplayOrder(), c.isActive());
        resp.children().addAll(c.getChildren().stream().map(Mapper::toCategory).toList());
        return resp;
    }

    public static CartResponse toCart(Cart cart) {
        List<CartResponse.CartItemResponse> items = cart.getItems().stream().map(ci -> {
            Product p = ci.getProduct();
            String image = p.getImages().stream()
                    .filter(ProductImage::isPrimary)
                    .findFirst()
                    .map(ProductImage::getUrl)
                    .orElse(p.getImages().stream().findFirst().map(ProductImage::getUrl).orElse(null));
            return new CartResponse.CartItemResponse(p.getId(), p.getName(), p.getSlug(), image, p.getPrice(),
                    ci.getQuantity(), p.getStock());
        }).toList();

        double subtotal = items.stream().mapToDouble(i -> i.price() * i.quantity()).sum();
        int itemCount = items.stream().mapToInt(CartResponse.CartItemResponse::quantity).sum();

        return new CartResponse(items, subtotal, itemCount);
    }

    public static AddressResponse toAddress(Address a) {
        return new AddressResponse(a.getId(), a.getLabel(), a.getStreet(), a.getWard(), a.getDistrict(),
                a.getCity(), a.getReceiverName(), a.getReceiverPhone(), a.isDefault());
    }

    public static OrderResponse toOrder(Order o) {
        Payment payment = o.getPayment();
        OrderResponse.PaymentResponse payResp = payment == null ? null
                : new OrderResponse.PaymentResponse(payment.getMethod(), payment.getStatus(), payment.getAmount(),
                        payment.getPayosOrderCode(), payment.getPaidAt());

        List<OrderResponse.OrderItemResponse> items = o.getItems().stream().map(oi -> {
            String image = oi.getProduct() == null ? null
                    : oi.getProduct().getImages().stream()
                            .filter(ProductImage::isPrimary)
                            .findFirst()
                            .map(ProductImage::getUrl)
                            .orElse(null);
            return new OrderResponse.OrderItemResponse(oi.getProduct() == null ? null : oi.getProduct().getId(),
                    oi.getProductNameSnapshot(), image, oi.getUnitPrice(), oi.getQuantity());
        }).toList();

        return new OrderResponse(o.getId(), o.getReceiverName(), o.getReceiverPhone(), o.getAddress(), o.getStatus(),
                o.getSubtotal(), o.getShippingFee(), o.getTotal(), o.getNotes(),
                o.getCreatedAt(), payResp, items);
    }

    public static ReviewResponse toReview(Review r) {
        return new ReviewResponse(r.getId(), r.getCustomer().getId(), r.getCustomer().getUsername(),
                r.getRating(), r.getContent(), r.isHidden(), r.getCreatedAt());
    }

    public static NotificationResponse toNotification(Notification n) {
        return new NotificationResponse(n.getId(), n.getTitle(), n.getMessage(), n.getType(), n.isRead(),
                n.getCreatedAt());
    }

    public static BannerResponse toBanner(Banner b) {
        return new BannerResponse(b.getId(), b.getTitle(), b.getSubtitle(), b.getImageUrl(), b.getLinkUrl(),
                b.getDisplayOrder(), b.isActive());
    }

    public static ChatResponse.MessageResponse toChatMessage(ChatMessage m) {
        return new ChatResponse.MessageResponse(m.getId(), m.getRole(), m.getContent(), m.getCreatedAt());
    }

    public static <T> PageResponse<T> toPage(org.springframework.data.domain.Page<?> page, List<T> content) {
        return new PageResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }

    public static void mergeProduct(Product product, ProductRequest req, Category category) {
        product.setName(req.name());
        product.setSlug(req.slug());
        product.setDescription(req.description());
        product.setPrice(req.price());
        product.setStock(req.stock());
        product.setWeight(req.weight() == null ? 0 : req.weight());
        product.setOrigin(req.origin());
        product.setCategory(category);
        product.setActive(req.active());

        product.getImages().clear();
        if (req.images() != null) {
            int order = 0;
            for (ProductRequest.ProductImageRequest img : req.images()) {
                ProductImage pi = new ProductImage();
                pi.setProduct(product);
                pi.setUrl(img.url());
                pi.setPrimary(img.primary());
                pi.setDisplayOrder(img.displayOrder() == null ? order : img.displayOrder());
                product.getImages().add(pi);
                order++;
            }
        }
    }

    public static ProductMaterial productMaterial(Product product, Material material, Double percentage) {
        ProductMaterial pm = new ProductMaterial();
        pm.setId(new ProductMaterialId(product.getId(), material.getId()));
        pm.setProduct(product);
        pm.setMaterial(material);
        pm.setPercentage(percentage);
        return pm;
    }

    public static void mergeCategory(Category category, CategoryRequest req, Category parent) {
        if (parent != null) {
            category.setParent(parent);
        }
        category.setName(req.name());
        category.setSlug(req.slug());
        category.setIcon(req.icon());
        category.setDisplayOrder(req.displayOrder() == null ? 0 : req.displayOrder());
        category.setActive(req.active());
    }

    public static void mergeBanner(Banner banner, BannerRequest req) {
        banner.setTitle(req.title());
        banner.setSubtitle(req.subtitle());
        banner.setImageUrl(req.imageUrl());
        banner.setLinkUrl(req.linkUrl());
        banner.setDisplayOrder(req.displayOrder() == null ? 0 : req.displayOrder());
        if (req.active() != null) {
            banner.setActive(req.active());
        }
    }

    public static void mergeAddress(Address address, AddressRequest req) {
        address.setLabel(req.label());
        address.setStreet(req.street());
        address.setWard(req.ward());
        address.setDistrict(req.district());
        address.setCity(req.city());
        address.setReceiverName(req.receiverName());
        address.setReceiverPhone(req.receiverPhone());
        address.setDefault(req.isDefault());
    }
}
