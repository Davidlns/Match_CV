package com.david.matchcv.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.david.matchcv.domain.EstratoConsenso;
import com.david.matchcv.domain.SkillAgregada;
import com.david.matchcv.domain.SinergiaVaga;
import com.david.matchcv.dto.AnaliseCompletaResponse;
import com.david.matchcv.exception.AiException;
import com.david.matchcv.exception.InvalidPdfException;
import com.david.matchcv.service.AnaliseCompletaService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AnaliseController.class)
class AnaliseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnaliseCompletaService analiseCompletaService;

    @Test
    void deveRetornar200ComAnaliseCompleta() throws Exception {
        AnaliseCompletaResponse resposta = new AnaliseCompletaResponse(
                3,
                List.of(new SkillAgregada("Java", 3, 3, 100, EstratoConsenso.PRATICAMENTE_TODAS)),
                List.of("Java"),
                List.of("Docker"),
                List.of("Python"),
                50,
                List.of(new SinergiaVaga(1, 50), new SinergiaVaga(2, 50), new SinergiaVaga(3, 50))
        );
        when(analiseCompletaService.analisar(any(), any())).thenReturn(resposta);

        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo", "cv.pdf", "application/pdf", "%PDF conteudo".getBytes());

        mockMvc.perform(multipart("/api/analise/completa")
                        .file(arquivo)
                        .param("descricoesVagas", "vaga 1", "vaga 2", "vaga 3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVagas").value(3))
                .andExpect(jsonPath("$.match[0]").value("Java"))
                .andExpect(jsonPath("$.gap[0]").value("Docker"))
                .andExpect(jsonPath("$.extra[0]").value("Python"))
                .andExpect(jsonPath("$.sinergiaMedia").value(50))
                .andExpect(jsonPath("$.skillsAgregadas[0].nome").value("Java"))
                .andExpect(jsonPath("$.skillsAgregadas[0].estrato").value("PRATICAMENTE_TODAS"));
    }

    @Test
    void deveRetornar400QuandoParamDescricoesVagasAusente() throws Exception {
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo", "cv.pdf", "application/pdf", "%PDF conteudo".getBytes());

        mockMvc.perform(multipart("/api/analise/completa").file(arquivo))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar400QuandoMenosDeTresVagas() throws Exception {
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo", "cv.pdf", "application/pdf", "%PDF conteudo".getBytes());

        mockMvc.perform(multipart("/api/analise/completa")
                        .file(arquivo)
                        .param("descricoesVagas", "vaga 1", "vaga 2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Envie entre 3 e 8 descrições de vaga."));
    }

    @Test
    void deveRetornar400QuandoMaisDeOitoVagas() throws Exception {
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo", "cv.pdf", "application/pdf", "%PDF conteudo".getBytes());

        mockMvc.perform(multipart("/api/analise/completa")
                        .file(arquivo)
                        .param("descricoesVagas",
                                "v1", "v2", "v3", "v4", "v5", "v6", "v7", "v8", "v9"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Envie entre 3 e 8 descrições de vaga."));
    }

    @Test
    void deveRetornar400QuandoPdfInvalido() throws Exception {
        when(analiseCompletaService.analisar(any(), any()))
                .thenThrow(new InvalidPdfException("Arquivo PDF inválido ou corrompido."));

        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo", "cv.txt", "text/plain", "nao e pdf".getBytes());

        mockMvc.perform(multipart("/api/analise/completa")
                        .file(arquivo)
                        .param("descricoesVagas", "vaga 1", "vaga 2", "vaga 3"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Arquivo PDF inválido ou corrompido."));
    }

    @Test
    void deveRetornar503QuandoAIaFalha() throws Exception {
        when(analiseCompletaService.analisar(any(), any()))
                .thenThrow(new AiException("falha", new RuntimeException()));

        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo", "cv.pdf", "application/pdf", "%PDF conteudo".getBytes());

        mockMvc.perform(multipart("/api/analise/completa")
                        .file(arquivo)
                        .param("descricoesVagas", "vaga 1", "vaga 2", "vaga 3"))
                .andExpect(status().isServiceUnavailable());
    }
}
