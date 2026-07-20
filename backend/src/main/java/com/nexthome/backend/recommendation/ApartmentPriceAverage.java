package com.nexthome.backend.recommendation;

import java.math.BigDecimal;

public record ApartmentPriceAverage(
        long apartmentId,
        long regionId,
        String apartmentName,
        String address,
        BigDecimal averagePricePerPyeong,
        int tradeCount) {
}
