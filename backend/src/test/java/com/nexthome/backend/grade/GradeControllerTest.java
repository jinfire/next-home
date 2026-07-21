package com.nexthome.backend.grade;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class GradeControllerTest {
    private GradeService service;
    private MockMvc mvc;
    @BeforeEach void setUp() { service = Mockito.mock(GradeService.class); mvc = MockMvcBuilders.standaloneSetup(new GradeController(service)).build(); }
    @Test void returnsAnnualGradesForMap() throws Exception {
        when(service.findByYear(2026)).thenReturn(List.of(new GradeSummary(1L, "11680", "강남구", 2026, new BigDecimal("90000000"), 1, 120)));
        mvc.perform(get("/api/grades").param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].grade").value(1))
                .andExpect(jsonPath("$[0].averagePricePerPyeong").value(90000000));
    }
    @Test void returnsOnlyYearsWithCalculatedGrades() throws Exception {
        when(service.availableYears()).thenReturn(List.of(2024, 2025, 2026));
        mvc.perform(get("/api/grades/years"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(2024))
                .andExpect(jsonPath("$[2]").value(2026));
    }
    @Test void returnsActuallyCollectedTradeMonths() throws Exception {
        when(service.tradeMonths(2026)).thenReturn(List.of("2026-01", "2026-06"));
        mvc.perform(get("/api/grades/coverage").param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("2026-01"))
                .andExpect(jsonPath("$[1]").value("2026-06"));
    }
}
