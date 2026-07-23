package com.david.matchcv.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.david.matchcv.domain.SkillExtraida;
import com.david.matchcv.domain.TipoSkill;
import com.david.matchcv.exception.AiException;
import com.david.matchcv.exception.InvalidPdfException;
import com.david.matchcv.service.CvAnalysisService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CvController.class)
class CvControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CvAnalysisService cvAnalysisService;

    @Test
    void deveRetornar200ComSkillsExtraidas() throws Exception {
        when(cvAnalysisService.extrairSkillsDoCv(any())).thenReturn(
                List.of(new SkillExtraida("Java", TipoSkill.OBRIGATORIA),
                        new SkillExtraida("Docker", TipoSkill.DIFERENCIAL)));

        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo", "cv.pdf", "application/pdf", "%PDF conteudo".getBytes());

        mockMvc.perform(multipart("/api/cv/extract").file(arquivo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skills[0].nome").value("Java"))
                .andExpect(jsonPath("$.skills[0].tipo").value("OBRIGATORIA"))
                .andExpect(jsonPath("$.skills[1].nome").value("Docker"))
                .andExpect(jsonPath("$.skills[1].tipo").value("DIFERENCIAL"));
    }

    @Test
    void deveRetornar400QuandoPdfInvalido() throws Exception {
        when(cvAnalysisService.extrairSkillsDoCv(any()))
                .thenThrow(new InvalidPdfException("Arquivo PDF inválido ou corrompido."));

        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo", "texto.txt", "text/plain", "nao e um pdf".getBytes());

        mockMvc.perform(multipart("/api/cv/extract").file(arquivo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Arquivo PDF inválido ou corrompido."));
    }

    @Test
    void deveRetornar503QuandoAIaFalha() throws Exception {
        when(cvAnalysisService.extrairSkillsDoCv(any()))
                .thenThrow(new AiException("falha", new RuntimeException()));

        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo", "cv.pdf", "application/pdf", "%PDF conteudo".getBytes());

        mockMvc.perform(multipart("/api/cv/extract").file(arquivo))
                .andExpect(status().isServiceUnavailable());
    }
}
