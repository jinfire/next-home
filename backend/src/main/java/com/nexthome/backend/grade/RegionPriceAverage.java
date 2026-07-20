package com.nexthome.backend.grade;
import java.math.BigDecimal;
public record RegionPriceAverage(Long regionId, String regionName, BigDecimal averagePricePerPyeong, int tradeCount) {}
