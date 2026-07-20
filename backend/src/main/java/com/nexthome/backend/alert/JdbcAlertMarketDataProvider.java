package com.nexthome.backend.alert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
class JdbcAlertMarketDataProvider implements AlertMarketDataProvider {
    private final JdbcTemplate jdbc;

    JdbcAlertMarketDataProvider(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<AlertMarketSnapshot> snapshot(AlertCondition condition, int year) {
        List<AnnualGap> history = condition.targetRegionId() != null
                ? regionHistory(condition.currentRegionId(), condition.targetRegionId())
                : gradeHistory(condition.currentRegionId(), condition.targetGrade());
        Optional<AnnualGap> current = history.stream().filter(value -> value.year() == year).findFirst();
        if (current.isEmpty()) return Optional.empty();
        List<BigDecimal> sorted = history.stream().map(AnnualGap::gapPercent).sorted(Comparator.naturalOrder()).toList();
        return Optional.of(new AlertMarketSnapshot(current.get().gapPercent(), percentile(current.get().gapPercent(), sorted)));
    }

    private List<AnnualGap> regionHistory(long currentRegionId, long targetRegionId) {
        return jdbc.query("""
                SELECT current_grade.year,
                       ((target_grade.average_price_per_pyeong - current_grade.average_price_per_pyeong)
                         / current_grade.average_price_per_pyeong * 100) AS gap_percent
                FROM region_grade current_grade
                JOIN region_grade target_grade ON target_grade.year = current_grade.year
                WHERE current_grade.region_id = ? AND target_grade.region_id = ?
                  AND current_grade.average_price_per_pyeong > 0
                """, (rs, row) -> new AnnualGap(rs.getInt("year"), rs.getBigDecimal("gap_percent")),
                currentRegionId, targetRegionId);
    }

    private List<AnnualGap> gradeHistory(long currentRegionId, int targetGrade) {
        return jdbc.query("""
                WITH target_grade AS (
                    SELECT year, AVG(average_price_per_pyeong) AS average_price_per_pyeong
                    FROM region_grade WHERE grade = ? GROUP BY year
                )
                SELECT current_grade.year,
                       ((target_grade.average_price_per_pyeong - current_grade.average_price_per_pyeong)
                         / current_grade.average_price_per_pyeong * 100) AS gap_percent
                FROM region_grade current_grade
                JOIN target_grade ON target_grade.year = current_grade.year
                WHERE current_grade.region_id = ? AND current_grade.average_price_per_pyeong > 0
                """, (rs, row) -> new AnnualGap(rs.getInt("year"), rs.getBigDecimal("gap_percent")),
                targetGrade, currentRegionId);
    }

    private BigDecimal percentile(BigDecimal current, List<BigDecimal> sorted) {
        if (sorted.size() <= 1) return BigDecimal.ZERO.setScale(2);
        long lower = sorted.stream().filter(value -> value.compareTo(current) < 0).count();
        return BigDecimal.valueOf(lower * 100).divide(BigDecimal.valueOf(sorted.size() - 1L), 2, RoundingMode.HALF_UP);
    }

    private record AnnualGap(int year, BigDecimal gapPercent) {
    }
}
