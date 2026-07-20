package com.nexthome.backend.geocoding;

import java.time.LocalDate;

public interface ApiUsageCounter {
    boolean incrementWithinLimit(String periodType, LocalDate periodStart, int limit);
}
