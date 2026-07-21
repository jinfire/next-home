package com.nexthome.backend.grade;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class GradeCalculatorTest {

    private final GradeCalculator calculator = new GradeCalculator();

    @Test
    void calculatesPricePerPyeongFromSquareMeters() {
        BigDecimal value = calculator.pricePerPyeong(1_000_000_000L, new BigDecimal("84"));
        assertThat(value).isBetween(new BigDecimal("39354500"), new BigDecimal("39355500"));
    }

    @Test
    void assignsGradesFromPriceIndexAgainstCapitalAreaMedian() {
        long[] values = {300, 225, 180, 150, 110, 90, 85, 70, 55, 40};
        List<RegionPriceAverage> averages = IntStream.range(0, values.length)
                .mapToObj(index -> new RegionPriceAverage((long) index + 1, "지역" + index,
                        BigDecimal.valueOf(values[index]), 10)).toList();

        List<CalculatedRegionGrade> grades = calculator.assignGrades(2026, averages);

        assertThat(grades).extracting(CalculatedRegionGrade::grade)
                .containsExactly(1, 2, 3, 4, 6, 7, 7, 8, 9, 10);
    }

    @Test
    void usesStableRegionIdOrderWhenAveragePricesAreEqual() {
        List<RegionPriceAverage> averages = List.of(
                new RegionPriceAverage(2L, "B", BigDecimal.TEN, 3),
                new RegionPriceAverage(1L, "A", BigDecimal.TEN, 3));

        assertThat(calculator.assignGrades(2026, averages))
                .extracting(CalculatedRegionGrade::regionId)
                .containsExactly(1L, 2L);
    }
}
