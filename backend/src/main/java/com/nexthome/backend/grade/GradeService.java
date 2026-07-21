package com.nexthome.backend.grade;

import java.util.List;
import com.nexthome.backend.region.RegionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GradeService {
    private final RegionGradeRepository grades; private final RegionRepository regions; private final GradeCalculator calculator;
    GradeService(RegionGradeRepository grades, RegionRepository regions, GradeCalculator calculator) { this.grades=grades; this.regions=regions; this.calculator=calculator; }
    @Transactional(readOnly = true)
    public List<GradeSummary> findByYear(int year) { return grades.findByYearOrderByGradeAscAveragePricePerPyeongDesc((short)year).stream().map(GradeSummary::from).toList(); }
    @Transactional(readOnly = true)
    public List<Integer> availableYears() { return grades.findAvailableYears().stream().map(Short::intValue).toList(); }
    @Transactional(readOnly = true)
    public List<String> tradeMonths(int year) { return grades.findTradeMonths(year); }
    @Transactional
    public List<GradeSummary> recalculate(int year) {
        List<RegionPriceAverage> averages = grades.calculateAverages(year).stream().map(v -> new RegionPriceAverage(v.getRegionId(), v.getRegionName(), v.getAveragePricePerPyeong(), v.getTradeCount())).toList();
        grades.deleteByYear((short)year);
        grades.flush();
        calculator.assignGrades(year, averages).forEach(value -> grades.save(RegionGrade.create(regions.findById(value.regionId()).orElseThrow(), value)));
        return findByYear(year);
    }
}
