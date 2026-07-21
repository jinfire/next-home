package com.nexthome.backend.recommendation;

import java.math.BigDecimal;

public record NearbyUpgradeRegion(
        long regionId,
        String regionName,
        int grade,
        BigDecimal averagePricePerPyeong) {
}
