package com.nexthome.backend.grade;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.nexthome.backend.region.RegionRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class GradeServiceTest {

    @Test
    void flushesDeletedGradesBeforeSavingReplacements() {
        RegionGradeRepository grades = mock(RegionGradeRepository.class);
        RegionRepository regions = mock(RegionRepository.class);
        GradeCalculator calculator = mock(GradeCalculator.class);
        when(grades.calculateAverages(2026)).thenReturn(List.of());
        when(calculator.assignGrades(2026, List.of())).thenReturn(List.of());

        new GradeService(grades, regions, calculator).recalculate(2026);

        InOrder order = inOrder(grades);
        order.verify(grades).deleteByYear((short) 2026);
        order.verify(grades).flush();
    }
}
