package com.nexthome.backend.grade;
import java.math.BigDecimal;
public record GradeSummary(Long regionId, String regionCode, String regionName, int year, BigDecimal averagePricePerPyeong, int grade, int tradeCount) {
    static GradeSummary from(RegionGrade value) { return new GradeSummary(value.region().id(), value.region().code(), value.region().name(), value.year(), value.average(), value.grade(), value.tradeCount()); }
}
