package com.ecomart.dto.response;

public record AddressResponse(
        Long id,
        String label,
        String street,
        String ward,
        String district,
        String city,
        String receiverName,
        String receiverPhone,
        boolean isDefault
) {
}
