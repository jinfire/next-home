package com.nexthome.backend.geocoding;

import java.math.BigDecimal;

public record GeocodingResult(
        String normalizedAddress,
        String roadAddress,
        BigDecimal longitude,
        BigDecimal latitude) {
}
