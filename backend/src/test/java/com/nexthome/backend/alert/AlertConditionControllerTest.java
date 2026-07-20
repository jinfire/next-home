package com.nexthome.backend.alert;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AlertConditionControllerTest {
    private AlertConditionService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        service = Mockito.mock(AlertConditionService.class);
        mvc = MockMvcBuilders.standaloneSetup(new AlertConditionController(service)).build();
    }

    @Test
    void registersGradeAlertCondition() throws Exception {
        UUID browserId = UUID.randomUUID();
        when(service.create(any())).thenReturn(new AlertConditionResponse(
                7, browserId, 10, null, 3, null, null, true));

        mvc.perform(post("/api/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"browserId":"%s","currentRegionId":10,"targetGrade":3}
                                """.formatted(browserId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.targetGrade").value(3))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void rejectsConditionWithoutTarget() throws Exception {
        mvc.perform(post("/api/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"browserId":"%s","currentRegionId":10}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isBadRequest());
    }
}
