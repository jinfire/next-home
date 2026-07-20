package com.nexthome.backend.alert;

import org.springframework.stereotype.Component;

@Component
public class AlertConditionMatcher {
    public boolean matches(AlertThresholds thresholds, AlertMarketSnapshot market) {
        if (thresholds.maximumGapPercent() == null && thresholds.maximumHistoricalPercentile() == null) {
            return false;
        }
        boolean gapMatches = thresholds.maximumGapPercent() == null
                || market.gapPercent().compareTo(thresholds.maximumGapPercent()) <= 0;
        boolean percentileMatches = thresholds.maximumHistoricalPercentile() == null
                || market.historicalPercentile().compareTo(thresholds.maximumHistoricalPercentile()) <= 0;
        return gapMatches && percentileMatches;
    }
}
