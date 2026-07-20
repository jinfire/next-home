package com.nexthome.backend.alert;

import java.math.BigDecimal;
import java.util.UUID;

public record AlertConditionResponse(
        long id,
        UUID browserId,
        long currentRegionId,
        Long targetRegionId,
        Integer targetGrade,
        BigDecimal targetGapPercent,
        BigDecimal historicalGapPercentile,
        boolean enabled) {
}
