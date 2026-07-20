package com.nexthome.backend.geocoding;

import org.springframework.stereotype.Component;

@Component
public class AddressNormalizer {
    public String normalize(String address) {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("address must not be blank");
        }
        return address.trim().replaceAll("\\s+", " ");
    }
}
