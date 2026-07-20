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
        List<RegionPriceAverage> sorted = source.stream()
                .sorted(Comparator.comparing(RegionPriceAverage::averagePricePerPyeong).reversed()
                        .thenComparing(RegionPriceAverage::regionId))
                .toList();
        List<CalculatedRegionGrade> result = new ArrayList<>(sorted.size());
        for (int index = 0; index < sorted.size(); index++) {
            RegionPriceAverage average = sorted.get(index);
            int grade = sorted.size() == 1 ? 1 : 1 + (index * 9 / (sorted.size() - 1));
            result.add(new CalculatedRegionGrade(average.regionId(), average.regionName(), year,
                    average.averagePricePerPyeong().setScale(2, RoundingMode.HALF_UP), grade, average.tradeCount()));
        }
        return List.copyOf(result);
    }
}
