package com.nexthome.backend.recommendation;

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

class RecommendationControllerTest {
    private RecommendationService service;
    private MockMvc mvc;
    @BeforeEach void setUp(){ service=Mockito.mock(RecommendationService.class); mvc=MockMvcBuilders.standaloneSetup(new RecommendationController(service)).build(); }
    @Test void returnsOneAndTwoGradeUpgradeOptions() throws Exception {
        var recommendation = new UpgradeRecommendation(5,4,2026,new BigDecimal("50000000"),new BigDecimal("70000000"),new BigDecimal("20000000"),new BigDecimal("25"),5);
        when(service.recommend(5,2026)).thenReturn(List.of(recommendation));
        mvc.perform(get("/api/recommendations/upgrades").param("currentGrade","5").param("year","2026"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].targetGrade").value(4))
                .andExpect(jsonPath("$[0].currentGapPerPyeong").value(20000000));
    }
    @Test void rejectsInvalidGrade() throws Exception {
        mvc.perform(get("/api/recommendations/upgrades").param("currentGrade","11").param("year","2026"))
                .andExpect(status().isBadRequest());
    }
}
