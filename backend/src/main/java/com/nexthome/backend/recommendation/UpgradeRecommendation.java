package com.nexthome.backend.recommendation;
import java.math.BigDecimal;
public record UpgradeRecommendation(int currentGrade, int targetGrade, int year,
        BigDecimal currentAveragePricePerPyeong, BigDecimal targetAveragePricePerPyeong,
        BigDecimal currentGapPerPyeong, BigDecimal historicalGapPercentile, int historicalYears) {}
