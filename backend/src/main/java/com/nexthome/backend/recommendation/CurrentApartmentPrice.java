package com.nexthome.backend.recommendation;

import java.math.BigDecimal;

public record CurrentApartmentPrice(
        long apartmentId,
        String apartmentName,
        BigDecimal averagePricePerPyeong,
        int tradeCount,
        String tradeMonth) {
}
