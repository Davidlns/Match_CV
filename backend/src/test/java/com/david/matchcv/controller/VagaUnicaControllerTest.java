package com.david.matchcv.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.david.matchcv.domain.CategoriaAtencao;
import com.david.matchcv.domain.FeedbackAts;
import com.david.matchcv.domain.ImpactoAtencao;
import com.david.matchcv.domain.PontoDeAtencao;
import com.david.matchcv.dto.AnaliseVagaUnicaResponse;
import com.david.matchcv.exception.AiException;
import com.david.matchcv.exception.InvalidPdfException;
import com.david.matchcv.service.AnaliseVagaUnicaService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(VagaUnicaController.class)
class VagaUnicaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnaliseVagaUnicaService analiseVagaUnicaService;

    @Test
    void deveRetornar200ComAnaliseCompleta() throws Exception {
        FeedbackAts feedbackAts = new FeedbackAts(
                78,
                List.of("Experiência sólida em Java"),
                List.of(new PontoDeAtencao(CategoriaAtencao.KEYWORDS, "Falta mencionar Docker", ImpactoAtencao.ALTO)),
                List.of("Adicionar Docker ao resumo de habilidades")
        );
        AnaliseVagaUnicaResponse resposta = new AnaliseVagaUnicaResponse(
                60,
                List.of("Java"),
                List.of("Docker"),
                List.of("Python"),
                "## Fase 1\n- Aprender Docker",
                feedbackAts
        );
        when(analiseVagaUnicaService.analisar(anyString(), any())).thenReturn(resposta);

        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo", "cv.pdf", "application/pdf", "%PDF conteudo".getBytes());

        mockMvc.perform(multipart("/api/analise/vaga-unica")
                        .file(arquivo)
                        .param("descricaoVaga", "Vaga de desenvolvedor Java com Docker"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sinergia").value(60))
                .andExpect(jsonPath("$.match[0]").value("Java"))
                .andExpect(jsonPath("$.gap[0]").value("Docker"))
                .andExpect(jsonPath("$.extra[0]").value("Python"))
                .andExpect(jsonPath("$.roadmapDirecionado").value("## Fase 1\n- Aprender Docker"))
                .andExpect(jsonPath("$.feedbackAts.pontuacaoEstimada").value(78))
                .andExpect(jsonPath("$.feedbackAts.pontosFavoraveis[0]").value("Experiência sólida em Java"))
                .andExpect(jsonPath("$.feedbackAts.pontosDeAtencao[0].categoria").value("KEYWORDS"))
                .andExpect(jsonPath("$.feedbackAts.pontosDeAtencao[0].impacto").value("ALTO"))
                .andExpect(jsonPath("$.feedbackAts.acoesPrioritarias[0]")
                        .value("Adicionar Docker ao resumo de habilidades"));
    }

    @Test
    void deveRetornar400QuandoDescricaoVagaAusente() throws Exception {
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo", "cv.pdf", "application/pdf", "%PDF conteudo".getBytes());

        mockMvc.perform(multipart("/api/analise/vaga-unica").file(arquivo))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar400QuandoPdfInvalido() throws Exception {
        when(analiseVagaUnicaService.analisar(anyString(), any()))
                .thenThrow(new InvalidPdfException("Arquivo PDF inválido ou corrompido."));

        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo", "cv.txt", "text/plain", "nao e pdf".getBytes());

        mockMvc.perform(multipart("/api/analise/vaga-unica")
                        .file(arquivo)
                        .param("descricaoVaga", "Vaga de desenvolvedor"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Arquivo PDF inválido ou corrompido."));
    }

    @Test
    void deveRetornar503QuandoAIaFalha() throws Exception {
        when(analiseVagaUnicaService.analisar(anyString(), any()))
                .thenThrow(new AiException("falha na IA", new RuntimeException()));

        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo", "cv.pdf", "application/pdf", "%PDF conteudo".getBytes());

        mockMvc.perform(multipart("/api/analise/vaga-unica")
                        .file(arquivo)
                        .param("descricaoVaga", "Vaga de desenvolvedor"))
                .andExpect(status().isServiceUnavailable());
    }
}
