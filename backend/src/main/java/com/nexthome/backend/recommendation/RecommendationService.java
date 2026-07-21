package com.nexthome.backend.recommendation;

import java.util.List;
import org.springframework.dao.EmptyResultDataAccessException;
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
        return calculator.calculate(currentGrade, year, gradeHistory());
    }

    @Transactional(readOnly = true)
    public RegionUpgradeComparison recommendRegion(long regionId, int year) {
        try {
            CurrentRegionGrade current = jdbc.queryForObject("""
                    SELECT r.name, rg.grade, rg.average_price_per_pyeong
                    FROM region_grade rg JOIN region r ON r.id=rg.region_id
                    WHERE rg.region_id=? AND rg.year=?
                    """, (rs, row) -> new CurrentRegionGrade(
                    rs.getString("name"), rs.getInt("grade"), rs.getBigDecimal("average_price_per_pyeong")),
                    regionId, year);
            List<NearbyUpgradeRegion> nearby = jdbc.query("""
                    SELECT candidate.id, candidate.name, candidate_grade.grade,
                           candidate_grade.average_price_per_pyeong,
                           history.min_additional_34,
                           history.max_additional_34,
                           history.observed_years
                    FROM region selected
                    JOIN region candidate ON candidate.id <> selected.id AND candidate.boundary IS NOT NULL
                    JOIN region_grade candidate_grade ON candidate_grade.region_id=candidate.id AND candidate_grade.year=?
                    LEFT JOIN LATERAL (
                        SELECT MIN(GREATEST(candidate_history.average_price_per_pyeong - selected_history.average_price_per_pyeong, 0) * 34) AS min_additional_34,
                               MAX(GREATEST(candidate_history.average_price_per_pyeong - selected_history.average_price_per_pyeong, 0) * 34) AS max_additional_34,
                               COUNT(*) AS observed_years
                        FROM region_grade candidate_history
                        JOIN region_grade selected_history ON selected_history.year = candidate_history.year
                        WHERE candidate_history.region_id = candidate.id
                          AND selected_history.region_id = selected.id
                    ) history ON TRUE
                    WHERE selected.id=? AND candidate_grade.grade BETWEEN GREATEST(1, ? - 2) AND ? - 1
                    ORDER BY ST_Distance(ST_Centroid(selected.boundary)::geography,
                                         ST_Centroid(candidate.boundary)::geography),
                             candidate_grade.grade
                    LIMIT 4
                    """, (rs, row) -> new NearbyUpgradeRegion(
                    rs.getLong("id"), rs.getString("name"), rs.getInt("grade"),
                    rs.getBigDecimal("average_price_per_pyeong"),
                    rs.getBigDecimal("min_additional_34"),
                    rs.getBigDecimal("max_additional_34"),
                    rs.getInt("observed_years")),
                    year, regionId, current.grade(), current.grade());
            return new RegionUpgradeComparison(regionId, current.name(), current.grade(), year, current.average(),
                    calculator.calculate(current.grade(), year, gradeHistory()), nearby);
        } catch (EmptyResultDataAccessException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "선택한 지역의 해당 연도 급지 데이터가 없습니다.");
        }
    }

    private List<AnnualGradeAverage> gradeHistory() {
        return jdbc.query("""
                SELECT year, grade, AVG(average_price_per_pyeong) AS grade_average
                FROM region_grade GROUP BY year, grade ORDER BY year, grade
                """, (rs, row) -> new AnnualGradeAverage(rs.getInt("year"), rs.getInt("grade"), rs.getBigDecimal("grade_average")));
    }

    @Transactional(readOnly = true)
    public List<LifestyleApartmentRecommendation> recommendApartments(long apartmentId, int year) {
        List<ApartmentPriceAverage> averages = jdbc.query("""
                WITH latest_month AS (
                    SELECT coverage.contract_month AS month_start
                    FROM trade_collection_coverage coverage
                    JOIN region covered_region ON covered_region.id = coverage.region_id
                    WHERE EXTRACT(YEAR FROM coverage.contract_month) = ?
                      AND covered_region.level = 2
                    GROUP BY coverage.contract_month
                    HAVING COUNT(DISTINCT coverage.region_id) = (
                        SELECT COUNT(*) FROM region WHERE level = 2
                    )
                    ORDER BY coverage.contract_month DESC
                    LIMIT 1
                )
                SELECT a.id AS apartment_id, a.region_id, a.name, a.road_address AS address,
                       AVG(t.price_krw * 3.305785 / t.exclusive_area_sqm) AS average_price_per_pyeong,
                       COUNT(*) AS trade_count,
                       TO_CHAR(latest_month.month_start, 'YYYY-MM') AS trade_month
                FROM apartment a
                JOIN trade t ON t.apartment_id = a.id
                CROSS JOIN latest_month
                WHERE t.contract_date >= latest_month.month_start
                  AND t.contract_date < latest_month.month_start + INTERVAL '1 month'
                  AND t.cancellation_date IS NULL
                GROUP BY a.id, a.region_id, a.name, a.road_address, latest_month.month_start
                """, (rs, row) -> new ApartmentPriceAverage(
                        rs.getLong("apartment_id"),
                        rs.getLong("region_id"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getBigDecimal("average_price_per_pyeong"),
                        rs.getInt("trade_count"),
                        rs.getString("trade_month")), year);

        ApartmentPriceAverage current = averages.stream()
                .filter(average -> average.apartmentId() == apartmentId)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No valid trades found for this apartment and year"));
        return apartmentCalculator.recommend(current, averages);
    }

    @Transactional(readOnly = true)
    public CurrentApartmentPrice currentApartmentPrice(long apartmentId, int year) {
        try {
            return jdbc.queryForObject("""
                    WITH latest_month AS (
                        SELECT coverage.contract_month AS month_start
                        FROM trade_collection_coverage coverage
                        JOIN region covered_region ON covered_region.id = coverage.region_id
                        WHERE EXTRACT(YEAR FROM coverage.contract_month) = ? AND covered_region.level = 2
                        GROUP BY coverage.contract_month
                        HAVING COUNT(DISTINCT coverage.region_id) = (SELECT COUNT(*) FROM region WHERE level = 2)
                        ORDER BY coverage.contract_month DESC LIMIT 1
                    )
                    SELECT a.id, a.name,
                           AVG(t.price_krw * 3.305785 / t.exclusive_area_sqm) AS average_price_per_pyeong,
                           COUNT(*) AS trade_count,
                           TO_CHAR(latest_month.month_start, 'YYYY-MM') AS trade_month
                    FROM apartment a
                    JOIN trade t ON t.apartment_id = a.id
                    CROSS JOIN latest_month
                    WHERE a.id = ?
                      AND t.contract_date >= latest_month.month_start
                      AND t.contract_date < latest_month.month_start + INTERVAL '1 month'
                      AND t.cancellation_date IS NULL
                    GROUP BY a.id, a.name, latest_month.month_start
                    """, (rs, row) -> new CurrentApartmentPrice(
                    rs.getLong("id"), rs.getString("name"), rs.getBigDecimal("average_price_per_pyeong"),
                    rs.getInt("trade_count"), rs.getString("trade_month")), year, apartmentId);
        } catch (EmptyResultDataAccessException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "최신 완결 월에 현재 아파트 실거래가 없습니다.");
        }
    }

    private record CurrentRegionGrade(String name, int grade, java.math.BigDecimal average) {
    }
}
