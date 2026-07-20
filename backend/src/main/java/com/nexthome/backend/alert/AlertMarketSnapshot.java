package com.nexthome.backend.alert;

import java.math.BigDecimal;

public record AlertMarketSnapshot(BigDecimal gapPercent, BigDecimal historicalPercentile) {
}
