package com.nexthome.backend.recommendation;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecommendationService {
    private final JdbcTemplate jdbc;
    private final UpgradeRecommendationCalculator calculator;
    public RecommendationService(JdbcTemplate jdbc, UpgradeRecommendationCalculator calculator) { this.jdbc=jdbc; this.calculator=calculator; }
    @Transactional(readOnly = true)
    public List<UpgradeRecommendation> recommend(int currentGrade, int year) {
        List<AnnualGradeAverage> history = jdbc.query("""
                SELECT year, grade, AVG(average_price_per_pyeong) AS grade_average
                FROM region_grade GROUP BY year, grade ORDER BY year, grade
                """, (rs, row) -> new AnnualGradeAverage(rs.getInt("year"), rs.getInt("grade"), rs.getBigDecimal("grade_average")));
        return calculator.calculate(currentGrade, year, history);
    }
}
