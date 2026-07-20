package com.nexthome.backend.geocoding;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class PersistentGeocodingBudgetTest {
    @Test
    void blocksWhenEitherTheDailyOrMonthlyLimitIsReached() {
        ApiUsageCounter counter = mock(ApiUsageCounter.class);
        Clock clock = Clock.fixed(Instant.parse("2026-07-20T00:00:00Z"), ZoneOffset.UTC);
        when(counter.incrementWithinLimit("DAY", java.time.LocalDate.of(2026, 7, 20), 900)).thenReturn(true);
        when(counter.incrementWithinLimit("MONTH", java.time.LocalDate.of(2026, 7, 1), 18000)).thenReturn(false);
        PersistentGeocodingBudget budget = new PersistentGeocodingBudget(counter, clock, 900, 18000);

        assertThatThrownBy(budget::reserve).isInstanceOf(ApiBudgetExceededException.class);
    }
}
