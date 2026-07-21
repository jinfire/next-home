package com.nexthome.backend.recommendation;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class LifestyleApartmentCalculatorTest {
    private final LifestyleApartmentCalculator calculator = new LifestyleApartmentCalculator();

    @Test
    void recommendsMoreExpensiveApartmentsOnlyInSameRegion() {
        ApartmentPriceAverage current = apt(1, 10, "Current", 50);
        List<ApartmentPriceAverage> candidates = List.of(
                apt(2, 10, "Upgrade A", 80), apt(3, 10, "Upgrade B", 60),
                apt(4, 10, "Lower", 40), apt(5, 20, "Other Region", 100));

        List<LifestyleApartmentRecommendation> result = calculator.recommend(current, candidates);

        assertThat(result).extracting(LifestyleApartmentRecommendation::apartmentName)
                .containsExactly("Upgrade B", "Upgrade A");
        assertThat(result).extracting(LifestyleApartmentRecommendation::gapPerPyeong)
                .containsExactly(new BigDecimal("10.00"), new BigDecimal("30.00"));
    }

    @Test
    void limitsRecommendationsToTen() {
        ApartmentPriceAverage current = apt(1, 10, "Current", 10);
        List<ApartmentPriceAverage> candidates = java.util.stream.IntStream.range(0, 15)
                .mapToObj(i -> apt(i + 2, 10, "Candidate " + i, 100 - i)).toList();
        assertThat(calculator.recommend(current, candidates)).hasSize(5);
    }

    private ApartmentPriceAverage apt(long id, long regionId, String name, long average) {
        return new ApartmentPriceAverage(id, regionId, name, name + " address", BigDecimal.valueOf(average), 3);
    }
}
