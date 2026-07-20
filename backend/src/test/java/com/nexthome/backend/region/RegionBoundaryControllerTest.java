package com.nexthome.backend.region;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class RegionBoundaryControllerTest {
    @Test
    void servesGeoJsonForTheRequestedYear() throws Exception {
        RegionBoundaryService service = Mockito.mock(RegionBoundaryService.class);
        when(service.findByYear(2026)).thenReturn("{\"type\":\"FeatureCollection\",\"features\":[]}");

        MockMvcBuilders.standaloneSetup(new RegionBoundaryController(service)).build()
                .perform(get("/api/region-boundaries").param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/geo+json"))
                .andExpect(content().json("{\"type\":\"FeatureCollection\",\"features\":[]}"));
    }
}
