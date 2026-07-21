package com.nexthome.backend.recommendation;

import java.math.BigDecimal;
import java.util.List;

public record RegionUpgradeComparison(
        long regionId,
        String regionName,
        int currentGrade,
        int year,
        BigDecimal currentAveragePricePerPyeong,
        List<UpgradeRecommendation> targets,
        List<NearbyUpgradeRegion> nearbyRegions) {
}
