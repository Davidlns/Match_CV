package com.david.matchcv.controller;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.david.matchcv.domain.EstratoConsenso;
import com.david.matchcv.domain.SkillAgregada;
import com.david.matchcv.exception.AiException;
import com.david.matchcv.service.VagaAnalysisService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AnalysisController.class)
class AnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VagaAnalysisService vagaAnalysisService;

    @Test
    void deveRetornar200ComAnaliseNoSucesso() throws Exception {
        when(vagaAnalysisService.analisar(anyList())).thenReturn(List.of(
                new SkillAgregada("Java", 3, 3, 100, EstratoConsenso.PRATICAMENTE_TODAS)
        ));

        mockMvc.perform(post("/api/skills/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricoesVagas\":[\"vaga A\",\"vaga B\",\"vaga C\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVagas").value(3))
                .andExpect(jsonPath("$.skills[0].nome").value("Java"))
                .andExpect(jsonPath("$.skills[0].obrigatoriaEm").value(3))
                .andExpect(jsonPath("$.skills[0].percentual").value(100))
                .andExpect(jsonPath("$.skills[0].estrato").value("PRATICAMENTE_TODAS"));
    }

    @Test
    void deveRetornar400QuandoListaVazia() throws Exception {
        mockMvc.perform(post("/api/skills/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricoesVagas\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void deveRetornar400QuandoMenosDeTresVagas() throws Exception {
        mockMvc.perform(post("/api/skills/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricoesVagas\":[\"vaga A\",\"vaga B\"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Envie entre 3 e 8 descrições de vaga."));
    }

    @Test
    void deveRetornar400QuandoMaisDeOitoVagas() throws Exception {
        String noveVagas = "[\"v1\",\"v2\",\"v3\",\"v4\",\"v5\",\"v6\",\"v7\",\"v8\",\"v9\"]";
        mockMvc.perform(post("/api/skills/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricoesVagas\":" + noveVagas + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Envie entre 3 e 8 descrições de vaga."));
    }

    @Test
    void deveRetornar503QuandoAIaFalha() throws Exception {
        when(vagaAnalysisService.analisar(anyList()))
                .thenThrow(new AiException("falha", new RuntimeException()));

        mockMvc.perform(post("/api/skills/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricoesVagas\":[\"vaga A\",\"vaga B\",\"vaga C\"]}"))
                .andExpect(status().isServiceUnavailable());
    }
}
