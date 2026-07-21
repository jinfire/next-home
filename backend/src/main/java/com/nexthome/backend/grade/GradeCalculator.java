package com.nexthome.backend.grade;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GradeCalculator {
    private static final BigDecimal SQUARE_METERS_PER_PYEONG = new BigDecimal("3.305785");

    public BigDecimal pricePerPyeong(long priceKrw, BigDecimal areaSqm) {
        if (priceKrw <= 0 || areaSqm == null || areaSqm.signum() <= 0) {
            throw new IllegalArgumentException("가격과 전용면적은 0보다 커야 합니다.");
        }
        return BigDecimal.valueOf(priceKrw).multiply(SQUARE_METERS_PER_PYEONG)
                .divide(areaSqm, 2, RoundingMode.HALF_UP);
    }

    public List<CalculatedRegionGrade> assignGrades(int year, List<RegionPriceAverage> source) {
        if (source.isEmpty()) return List.of();
        List<RegionPriceAverage> sorted = source.stream()
                .sorted(Comparator.comparing(RegionPriceAverage::averagePricePerPyeong).reversed()
                        .thenComparing(RegionPriceAverage::regionId))
                .toList();
        List<BigDecimal> prices = source.stream().map(RegionPriceAverage::averagePricePerPyeong).sorted().toList();
        BigDecimal median = prices.size() % 2 == 1
                ? prices.get(prices.size() / 2)
                : prices.get(prices.size() / 2 - 1).add(prices.get(prices.size() / 2))
                        .divide(BigDecimal.valueOf(2), 8, RoundingMode.HALF_UP);
        List<CalculatedRegionGrade> result = new ArrayList<>(sorted.size());
        for (int index = 0; index < sorted.size(); index++) {
            RegionPriceAverage average = sorted.get(index);
            BigDecimal priceIndex = average.averagePricePerPyeong()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(median, 4, RoundingMode.HALF_UP);
            int grade = gradeFor(priceIndex);
            result.add(new CalculatedRegionGrade(average.regionId(), average.regionName(), year,
                    average.averagePricePerPyeong().setScale(2, RoundingMode.HALF_UP), grade, average.tradeCount()));
        }
        return List.copyOf(result);
    }

    private int gradeFor(BigDecimal index) {
        if (index.compareTo(BigDecimal.valueOf(250)) >= 0) return 1;
        if (index.compareTo(BigDecimal.valueOf(200)) >= 0) return 2;
        if (index.compareTo(BigDecimal.valueOf(165)) >= 0) return 3;
        if (index.compareTo(BigDecimal.valueOf(140)) >= 0) return 4;
        if (index.compareTo(BigDecimal.valueOf(120)) >= 0) return 5;
        if (index.compareTo(BigDecimal.valueOf(100)) >= 0) return 6;
        if (index.compareTo(BigDecimal.valueOf(85)) >= 0) return 7;
        if (index.compareTo(BigDecimal.valueOf(70)) >= 0) return 8;
        if (index.compareTo(BigDecimal.valueOf(55)) >= 0) return 9;
        return 10;
    }
}
