package com.nexthome.backend.grade;
import java.math.BigDecimal;
public record CalculatedRegionGrade(Long regionId, String regionName, int year, BigDecimal averagePricePerPyeong, int grade, int tradeCount) {}
