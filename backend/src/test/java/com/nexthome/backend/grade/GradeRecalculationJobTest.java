package com.nexthome.backend.grade;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.jdbc.core.JdbcTemplate;

class GradeRecalculationJobTest {

    @Test
    void recalculatesEveryYearThatHasTradesForTheDedicatedCommand() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        GradeService grades = mock(GradeService.class);
        when(jdbc.queryForList(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.eq(Integer.class)))
                .thenReturn(List.of(2025, 2026));

        new GradeRecalculationJob(jdbc, grades)
                .run(new DefaultApplicationArguments("--recalculate-grades"));

        verify(grades).recalculate(2025);
        verify(grades).recalculate(2026);
    }
}
