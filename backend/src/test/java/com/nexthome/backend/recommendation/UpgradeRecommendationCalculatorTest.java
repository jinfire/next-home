package com.nexthome.backend.recommendation;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class UpgradeRecommendationCalculatorTest {
    private final UpgradeRecommendationCalculator calculator = new UpgradeRecommendationCalculator();

    @Test
    void recommendsOneAndTwoGradesAboveWithCurrentGap() {
        List<AnnualGradeAverage> history = List.of(
                avg(2025, 5, 45), avg(2025, 4, 65), avg(2025, 3, 80),
                avg(2026, 5, 50), avg(2026, 4, 70), avg(2026, 3, 90));

        List<UpgradeRecommendation> result = calculator.calculate(5, 2026, history);

        assertThat(result).extracting(UpgradeRecommendation::targetGrade).containsExactly(4, 3);
        assertThat(result).extracting(UpgradeRecommendation::currentGapPerPyeong)
                .containsExactly(new BigDecimal("20.00"), new BigDecimal("40.00"));
    }

    @Test
    void expressesCurrentGapPositionBetweenHistoricalMinimumAndMaximum() {
        List<AnnualGradeAverage> history = List.of(
                avg(2024, 5, 50), avg(2024, 4, 60),
                avg(2025, 5, 50), avg(2025, 4, 80),
                avg(2026, 5, 50), avg(2026, 4, 70));

        UpgradeRecommendation result = calculator.calculate(5, 2026, history).get(0);

        assertThat(result.historicalGapPercentile()).isEqualByComparingTo("50.00");
        assertThat(result.historicalYears()).isEqualTo(3);
    }

    @Test
    void doesNotRecommendAboveGradeOne() {
        assertThat(calculator.calculate(1, 2026, List.of(avg(2026, 1, 100)))).isEmpty();
    }

    private AnnualGradeAverage avg(int year, int grade, long average) {
        return new AnnualGradeAverage(year, grade, BigDecimal.valueOf(average));
    }
}
