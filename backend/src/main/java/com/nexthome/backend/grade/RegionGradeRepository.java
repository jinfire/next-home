package com.nexthome.backend.grade;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface RegionGradeRepository extends JpaRepository<RegionGrade, Long> {
    List<RegionGrade> findByYearOrderByGradeAscAveragePricePerPyeongDesc(short year);
    @Query("SELECT DISTINCT grade.year FROM RegionGrade grade ORDER BY grade.year")
    List<Short> findAvailableYears();
    @Modifying void deleteByYear(short year);
    @Query(value = """
        SELECT r.id AS regionId, r.name AS regionName,
               AVG((t.price_krw * 3.305785) / t.exclusive_area_sqm) AS averagePricePerPyeong,
               COUNT(*) AS tradeCount
        FROM trade t JOIN apartment a ON a.id=t.apartment_id JOIN region r ON r.id=a.region_id
        WHERE EXTRACT(YEAR FROM t.contract_date)=:year AND t.cancellation_date IS NULL
        GROUP BY r.id, r.name
        """, nativeQuery = true)
    List<RegionAverageView> calculateAverages(@Param("year") int year);
}
