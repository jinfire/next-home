package com.nexthome.backend.recommendation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class UpgradeRecommendationCalculator {
    public List<UpgradeRecommendation> calculate(int currentGrade, int year, List<AnnualGradeAverage> history) {
        if (currentGrade < 1 || currentGrade > 10) throw new IllegalArgumentException("현재 급지는 1~10이어야 합니다.");
        Map<String, AnnualGradeAverage> indexed = history.stream().collect(Collectors.toMap(this::key, Function.identity()));
        AnnualGradeAverage current = indexed.get(key(year, currentGrade));
        if (current == null) return List.of();
        List<UpgradeRecommendation> result = new ArrayList<>(2);
        for (int targetGrade : new int[]{currentGrade - 1, currentGrade - 2}) {
            if (targetGrade < 1) continue;
            AnnualGradeAverage target = indexed.get(key(year, targetGrade));
            if (target == null) continue;
            BigDecimal currentGap = gap(current, target);
            List<BigDecimal> historicalGaps = history.stream().map(AnnualGradeAverage::year).distinct()
                    .map(historyYear -> gap(indexed.get(key(historyYear, currentGrade)), indexed.get(key(historyYear, targetGrade))))
                    .filter(value -> value != null).sorted(Comparator.naturalOrder()).toList();
            result.add(new UpgradeRecommendation(currentGrade, targetGrade, year,
                    scale(current.averagePricePerPyeong()), scale(target.averagePricePerPyeong()), scale(currentGap),
                    percentile(currentGap, historicalGaps), historicalGaps.size()));
        }
        return List.copyOf(result);
    }

    private BigDecimal gap(AnnualGradeAverage current, AnnualGradeAverage target) {
        return current == null || target == null ? null : target.averagePricePerPyeong().subtract(current.averagePricePerPyeong());
    }
    private BigDecimal percentile(BigDecimal value, List<BigDecimal> sorted) {
        if (sorted.size() <= 1) return BigDecimal.ZERO.setScale(2);
        int firstIndex = 0;
        while (firstIndex < sorted.size() && sorted.get(firstIndex).compareTo(value) < 0) firstIndex++;
        return BigDecimal.valueOf(firstIndex * 100L).divide(BigDecimal.valueOf(sorted.size() - 1L), 2, RoundingMode.HALF_UP);
    }
    private BigDecimal scale(BigDecimal value) { return value.setScale(2, RoundingMode.HALF_UP); }
    private String key(AnnualGradeAverage value) { return key(value.year(), value.grade()); }
    private String key(int year, int grade) { return year + ":" + grade; }
}
