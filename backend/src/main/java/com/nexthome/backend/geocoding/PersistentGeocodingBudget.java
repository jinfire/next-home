package com.nexthome.backend.geocoding;

import java.time.Clock;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PersistentGeocodingBudget implements GeocodingBudget {
    private final ApiUsageCounter counter;
    private final Clock clock;
    private final int dailyLimit;
    private final int monthlyLimit;

    PersistentGeocodingBudget(
            ApiUsageCounter counter,
            Clock clock,
            @Value("${app.naver.geocoding.daily-limit:900}") int dailyLimit,
            @Value("${app.naver.geocoding.monthly-limit:18000}") int monthlyLimit) {
        this.counter = counter;
        this.clock = clock;
        this.dailyLimit = dailyLimit;
        this.monthlyLimit = monthlyLimit;
    }

    @Override
    @Transactional
    public void reserve() {
        LocalDate today = LocalDate.now(clock);
        if (!counter.incrementWithinLimit("DAY", today, dailyLimit)) {
            throw new ApiBudgetExceededException("NAVER Geocoding daily budget exhausted");
        }
        LocalDate month = today.withDayOfMonth(1);
        if (!counter.incrementWithinLimit("MONTH", month, monthlyLimit)) {
            throw new ApiBudgetExceededException("NAVER Geocoding monthly budget exhausted");
        }
    }
}
