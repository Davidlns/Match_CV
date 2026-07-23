package com.david.matchcv.controller;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.david.matchcv.domain.Prioridade;
import com.david.matchcv.domain.SkillPrioridade;
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
                new SkillPrioridade("Java", 2, 2, 100, Prioridade.ALTA)
        ));

        mockMvc.perform(post("/api/skills/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricoesVagas\":[\"vaga A\",\"vaga B\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVagas").value(2))
                .andExpect(jsonPath("$.skills[0].nome").value("Java"))
                .andExpect(jsonPath("$.skills[0].obrigatoriaEm").value(2))
                .andExpect(jsonPath("$.skills[0].percentual").value(100))
                .andExpect(jsonPath("$.skills[0].prioridade").value("ALTA"));
    }

    @Test
    void deveFuncionarComUmaVaga() throws Exception {
        when(vagaAnalysisService.analisar(anyList())).thenReturn(List.of(
                new SkillPrioridade("Java", 1, 1, 100, Prioridade.ALTA)
        ));

        mockMvc.perform(post("/api/skills/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricoesVagas\":[\"vaga unica\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVagas").value(1))
                .andExpect(jsonPath("$.skills[0].prioridade").value("ALTA"));
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
    void deveRetornar503QuandoAIaFalha() throws Exception {
        when(vagaAnalysisService.analisar(anyList()))
                .thenThrow(new AiException("falha", new RuntimeException()));

        mockMvc.perform(post("/api/skills/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricoesVagas\":[\"vaga A\"]}"))
                .andExpect(status().isServiceUnavailable());
    }
}
