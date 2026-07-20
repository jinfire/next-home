package com.nexthome.backend.region;

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

class RegionControllerTest {
    private RegionSearchService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        service = Mockito.mock(RegionSearchService.class);
        mvc = MockMvcBuilders.standaloneSetup(new RegionController(service)).build();
    }

    @Test
    void searchesRegionsByName() throws Exception {
        when(service.search("강남")).thenReturn(List.of(new RegionSummary(1L, "11680", "강남구", 2)));

        mvc.perform(get("/api/regions").param("query", "강남"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("11680"))
                .andExpect(jsonPath("$[0].name").value("강남구"))
                .andExpect(jsonPath("$[0].level").value(2));
    }

    @Test
    void rejectsBlankSearchQuery() throws Exception {
        mvc.perform(get("/api/regions").param("query", "  "))
                .andExpect(status().isBadRequest());
    }
}
