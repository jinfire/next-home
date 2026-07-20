package com.nexthome.backend.recommendation;

import java.math.BigDecimal;

public record LifestyleApartmentRecommendation(
        long apartmentId,
        String apartmentName,
        String address,
        BigDecimal averagePricePerPyeong,
        BigDecimal gapPerPyeong,
        int tradeCount) {
}
