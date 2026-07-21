package com.nexthome.backend.recommendation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class LifestyleApartmentCalculator {
    private static final int MAX_RECOMMENDATIONS = 5;

    public List<LifestyleApartmentRecommendation> recommend(
            ApartmentPriceAverage current,
            List<ApartmentPriceAverage> candidates) {
        return candidates.stream()
                .filter(candidate -> candidate.apartmentId() != current.apartmentId())
                .filter(candidate -> candidate.regionId() == current.regionId())
                .filter(candidate -> candidate.averagePricePerPyeong()
                        .compareTo(current.averagePricePerPyeong()) > 0)
                .sorted(Comparator.comparing(ApartmentPriceAverage::averagePricePerPyeong)
                        .thenComparingLong(ApartmentPriceAverage::apartmentId))
                .limit(MAX_RECOMMENDATIONS)
                .map(candidate -> toRecommendation(current, candidate))
                .toList();
    }

    private LifestyleApartmentRecommendation toRecommendation(
            ApartmentPriceAverage current,
            ApartmentPriceAverage candidate) {
        BigDecimal average = candidate.averagePricePerPyeong().setScale(2, RoundingMode.HALF_UP);
        BigDecimal gap = candidate.averagePricePerPyeong()
                .subtract(current.averagePricePerPyeong())
                .setScale(2, RoundingMode.HALF_UP);
        return new LifestyleApartmentRecommendation(
                candidate.apartmentId(), candidate.apartmentName(), candidate.address(),
                average, gap, candidate.tradeCount());
    }
}
