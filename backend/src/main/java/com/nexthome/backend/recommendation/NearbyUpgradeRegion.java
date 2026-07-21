package com.nexthome.backend.recommendation;

import java.math.BigDecimal;

public record NearbyUpgradeRegion(
        long regionId,
        String regionName,
        int grade,
        BigDecimal averagePricePerPyeong,
        BigDecimal historicalMinAdditionalFor34Pyeong,
        BigDecimal historicalMaxAdditionalFor34Pyeong,
        int historicalYears) {
}
