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
    void listsCapitalRegionsGroupedByProvince() throws Exception {
        when(service.options()).thenReturn(List.of(
                new RegionOptionGroup(10L, "11", "서울특별시", List.of(
                        new RegionOption(1L, "11110", "종로구"),
                        new RegionOption(2L, "11680", "강남구"))),
                new RegionOptionGroup(20L, "41", "경기도", List.of(
                        new RegionOption(3L, "41135", "성남시 분당구")))));

        mvc.perform(get("/api/regions/options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("서울특별시"))
                .andExpect(jsonPath("$[0].regions[1].name").value("강남구"))
                .andExpect(jsonPath("$[1].regions[0].name").value("성남시 분당구"));
    }

    @Test
    void rejectsBlankSearchQuery() throws Exception {
        mvc.perform(get("/api/regions").param("query", "  "))
                .andExpect(status().isBadRequest());
    }
}
