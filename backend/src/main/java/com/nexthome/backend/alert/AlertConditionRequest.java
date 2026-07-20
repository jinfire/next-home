package com.nexthome.backend.alert;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record AlertConditionRequest(
        @NotNull UUID browserId,
        @Positive long currentRegionId,
        @Positive Long targetRegionId,
        @Min(1) @Max(10) Integer targetGrade,
        BigDecimal targetGapPercent,
        @Min(0) @Max(100) BigDecimal historicalGapPercentile) {

    @AssertTrue(message = "targetRegionId or targetGrade is required")
    public boolean isTargetPresent() {
        return targetRegionId != null || targetGrade != null;
    }
}
