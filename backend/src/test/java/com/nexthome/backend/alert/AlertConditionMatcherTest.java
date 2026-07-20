package com.nexthome.backend.alert;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class AlertConditionMatcherTest {
    private final AlertConditionMatcher matcher = new AlertConditionMatcher();

    @Test
    void matchesWhenAllConfiguredThresholdsAreMet() {
        var thresholds = new AlertThresholds(new BigDecimal("20"), new BigDecimal("30"));
        var market = new AlertMarketSnapshot(new BigDecimal("18"), new BigDecimal("25"));

        assertThat(matcher.matches(thresholds, market)).isTrue();
    }

    @Test
    void doesNotMatchWithoutAThresholdOrWhenOneThresholdFails() {
        var market = new AlertMarketSnapshot(new BigDecimal("18"), new BigDecimal("40"));

        assertThat(matcher.matches(new AlertThresholds(null, null), market)).isFalse();
        assertThat(matcher.matches(new AlertThresholds(new BigDecimal("20"), new BigDecimal("30")), market)).isFalse();
    }
}
