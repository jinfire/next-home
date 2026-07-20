package com.nexthome.backend.recommendation;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RecommendationService {
    private final JdbcTemplate jdbc;
    private final UpgradeRecommendationCalculator calculator;
    private final LifestyleApartmentCalculator apartmentCalculator;

    public RecommendationService(
            JdbcTemplate jdbc,
            UpgradeRecommendationCalculator calculator,
            LifestyleApartmentCalculator apartmentCalculator) {
        this.jdbc = jdbc;
        this.calculator = calculator;
        this.apartmentCalculator = apartmentCalculator;
    }
    @Transactional(readOnly = true)
    public List<UpgradeRecommendation> recommend(int currentGrade, int year) {
        List<AnnualGradeAverage> history = jdbc.query("""
                SELECT year, grade, AVG(average_price_per_pyeong) AS grade_average
                FROM region_grade GROUP BY year, grade ORDER BY year, grade
                """, (rs, row) -> new AnnualGradeAverage(rs.getInt("year"), rs.getInt("grade"), rs.getBigDecimal("grade_average")));
        return calculator.calculate(currentGrade, year, history);
    }

    @Transactional(readOnly = true)
    public List<LifestyleApartmentRecommendation> recommendApartments(long apartmentId, int year) {
        List<ApartmentPriceAverage> averages = jdbc.query("""
                SELECT a.id AS apartment_id, a.region_id, a.name, a.address,
                       AVG(t.price_krw * 3.305785 / t.exclusive_area_sqm) AS average_price_per_pyeong,
                       COUNT(*) AS trade_count
                FROM apartment a
                JOIN trade t ON t.apartment_id = a.id
                WHERE EXTRACT(YEAR FROM t.contract_date) = ?
                  AND t.cancellation_date IS NULL
                GROUP BY a.id, a.region_id, a.name, a.address
                """, (rs, row) -> new ApartmentPriceAverage(
                        rs.getLong("apartment_id"),
                        rs.getLong("region_id"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getBigDecimal("average_price_per_pyeong"),
                        rs.getInt("trade_count")), year);

        ApartmentPriceAverage current = averages.stream()
                .filter(average -> average.apartmentId() == apartmentId)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No valid trades found for this apartment and year"));
        return apartmentCalculator.recommend(current, averages);
    }
}
