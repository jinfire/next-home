package com.nexthome.backend.apartment;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ApartmentControllerTest {
    private ApartmentSearchService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        service = Mockito.mock(ApartmentSearchService.class);
        mvc = MockMvcBuilders.standaloneSetup(new ApartmentController(service)).build();
    }

    @Test
    void searchesApartmentsWithOptionalRegionFilter() throws Exception {
        when(service.search("래미안", 1L)).thenReturn(List.of(
                new ApartmentSummary(10L, "래미안 원베일리", "서울특별시 서초구 반포대로 1", 1L, "서초구", 2023)));

        mvc.perform(get("/api/apartments").param("query", "래미안").param("regionId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].address").doesNotExist())
                .andExpect(jsonPath("$[0].regionName").value("서초구"))
                .andExpect(jsonPath("$[0].buildYear").value(2023));
    }

    @Test
    void rejectsBlankQuery() throws Exception {
        mvc.perform(get("/api/apartments").param("query", " "))
                .andExpect(status().isBadRequest());
    }
}
