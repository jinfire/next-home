package com.nexthome.backend.push;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

class PushSubscriptionControllerTest {
    private PushSubscriptionService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        service = Mockito.mock(PushSubscriptionService.class);
        mvc = MockMvcBuilders.standaloneSetup(new PushSubscriptionController(service, "public-key")).build();
    }

    @Test
    void exposesThePublicVapidKey() throws Exception {
        mvc.perform(get("/api/push-subscriptions/vapid-public-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicKey").value("public-key"));
    }

    @Test
    void registersABrowserPushSubscription() throws Exception {
        UUID browserId = UUID.randomUUID();
        when(service.register(any())).thenReturn(new PushSubscriptionResponse(3, browserId, "https://push.example/1"));

        mvc.perform(post("/api/push-subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "browserId":"%s",
                                  "endpoint":"https://push.example/1",
                                  "p256dh":"browser-public-key",
                                  "auth":"browser-auth-key"
                                }
                                """.formatted(browserId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.browserId").value(browserId.toString()))
                .andExpect(jsonPath("$.endpoint").value("https://push.example/1"));
    }

    @Test
    void rejectsAnIncompleteSubscription() throws Exception {
        mvc.perform(post("/api/push-subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
