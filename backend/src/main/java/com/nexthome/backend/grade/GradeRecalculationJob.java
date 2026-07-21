package com.nexthome.backend.grade;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class GradeRecalculationJob implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(GradeRecalculationJob.class);
    private final JdbcTemplate jdbc;
    private final GradeService grades;

    public GradeRecalculationJob(JdbcTemplate jdbc, GradeService grades) {
        this.jdbc = jdbc;
        this.grades = grades;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!args.containsOption("recalculate-grades")) return;
        List<Integer> years = jdbc.queryForList("""
                SELECT DISTINCT EXTRACT(YEAR FROM contract_date)::int
                FROM trade ORDER BY 1
                """, Integer.class);
        for (int year : years) {
            int regions = grades.recalculate(year).size();
            log.info("수도권 급지 재계산 완료: year={}, regions={}", year, regions);
        }
    }
}
