package com.nexthome.backend.trade;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class TradeControllerTest {
    private TradeQueryService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        service = Mockito.mock(TradeQueryService.class);
        mvc = MockMvcBuilders.standaloneSetup(new TradeController(service)).build();
    }

    @Test
    void returnsRecentApartmentTrades() throws Exception {
        when(service.recentByApartment(10L)).thenReturn(List.of(new TradeSummary(
                20L, LocalDate.of(2026, 1, 5), 1_250_000_000L, new BigDecimal("84.91"), 12, false)));

        mvc.perform(get("/api/apartments/10/trades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].priceKrw").value(1_250_000_000L))
                .andExpect(jsonPath("$[0].exclusiveAreaSqm").value(84.91))
                .andExpect(jsonPath("$[0].cancelled").value(false));
    }
}
