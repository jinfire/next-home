package com.nexthome.backend.alert;

import java.math.BigDecimal;

public record AlertThresholds(BigDecimal maximumGapPercent, BigDecimal maximumHistoricalPercentile) {
}
