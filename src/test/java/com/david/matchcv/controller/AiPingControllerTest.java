package com.david.matchcv.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.david.matchcv.exception.AiException;
import com.david.matchcv.service.AiPingService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// Sobe só a camada web do AiPingController (rápido, sem contexto completo).
@WebMvcTest(AiPingController.class)
class AiPingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Substitui o Service real por um dublê controlado.
    @MockitoBean
    private AiPingService aiPingService;

    @Test
    void deveRetornar200EOReplyNoSucesso() throws Exception {
        when(aiPingService.ping()).thenReturn("pong");

        mockMvc.perform(get("/api/ai/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").value("pong"));
    }

    @Test
    void deveRetornar503QuandoServicoLancaAiException() throws Exception {
        when(aiPingService.ping()).thenThrow(new AiException("IA indisponível", new RuntimeException()));

        mockMvc.perform(get("/api/ai/ping"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("IA indisponível"));
    }
}
