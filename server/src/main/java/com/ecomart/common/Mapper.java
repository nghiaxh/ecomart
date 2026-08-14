package com.ecomart.common;

import com.ecomart.dto.response.*;
import com.ecomart.domain.entity.*;
import com.ecomart.domain.enums.NotificationType;

import java.util.List;

public final class Mapper {

    private Mapper() {
    }

    public static AuthResponse toAuth(User user, String token) {
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getEmail(),
                user.getNumberPhone(), user.getAvatarUrl(), user.getRole());
    }

    public static ProfileResponse toProfile(User user, Integer ecoPoints, Double totalCo2Saved) {
        return new ProfileResponse(user.getId(), user.getUsername(), user.getEmail(), user.getNumberPhone(),
                user.getAvatarUrl(), user.getRole(), user.getCreatedAt(), ecoPoints, totalCo2Saved);
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

        double co2Saved = Math.max(0, p.getBaselineCarbonIndex() - p.getCarbonIndex());

        return new ProductResponse(p.getId(), p.getName(), p.getSlug(), p.getDescription(), p.getPrice(),
                p.getStock(), p.getCarbonIndex(), p.getBaselineCarbonIndex(), co2Saved, p.getEcoPointsPerUnit(),
                p.getWeight(), p.getOrigin(),
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
            double co2Saved = Math.max(0, p.getBaselineCarbonIndex() - p.getCarbonIndex());
            return new CartResponse.CartItemResponse(p.getId(), p.getName(), p.getSlug(), image, p.getPrice(),
                    ci.getQuantity(), co2Saved, p.getStock());
        }).toList();

        double subtotal = items.stream().mapToDouble(i -> i.price() * i.quantity()).sum();
        double totalCo2Saved = items.stream().mapToDouble(i -> i.co2SavedPerUnit() * i.quantity()).sum();
        int itemCount = items.stream().mapToInt(CartResponse.CartItemResponse::quantity).sum();

        return new CartResponse(items, subtotal, totalCo2Saved, itemCount);
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
                    oi.getProductNameSnapshot(), image, oi.getUnitPrice(), oi.getQuantity(), oi.getUnitCo2Saved());
        }).toList();

        return new OrderResponse(o.getId(), o.getReceiverName(), o.getReceiverPhone(), o.getAddress(), o.getStatus(),
                o.getSubtotal(), o.getShippingFee(), o.getTotal(), o.getEcoPointsEarned(), o.getNotes(),
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

    public static Notification newNotification(User user, String title, String message, NotificationType type, String referenceId) {
        Notification n = new Notification();
        n.setUser(user);
        n.setTitle(title);
        n.setMessage(message);
        n.setType(type);
        n.setReferenceId(referenceId);
        return n;
    }
}
