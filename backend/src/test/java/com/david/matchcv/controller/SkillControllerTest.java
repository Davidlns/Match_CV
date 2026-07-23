package com.david.matchcv.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.david.matchcv.domain.SkillExtraida;
import com.david.matchcv.domain.TipoSkill;
import com.david.matchcv.exception.AiException;
import com.david.matchcv.service.SkillExtractionService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SkillController.class)
class SkillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SkillExtractionService skillExtractionService;

    @Test
    void deveRetornar200ComSkillsETiposNoSucesso() throws Exception {
        when(skillExtractionService.extrairSkills(anyString())).thenReturn(List.of(
                new SkillExtraida("React", TipoSkill.OBRIGATORIA),
                new SkillExtraida("Kafka", TipoSkill.DIFERENCIAL)
        ));

        mockMvc.perform(post("/api/skills/extract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricaoVaga\":\"Vaga React; Kafka e diferencial\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skills[0].nome").value("React"))
                .andExpect(jsonPath("$.skills[0].tipo").value("OBRIGATORIA"))
                .andExpect(jsonPath("$.skills[1].nome").value("Kafka"))
                .andExpect(jsonPath("$.skills[1].tipo").value("DIFERENCIAL"));
    }

    @Test
    void deveRetornar400QuandoDescricaoVazia() throws Exception {
        mockMvc.perform(post("/api/skills/extract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricaoVaga\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void deveRetornar503QuandoAIaFalha() throws Exception {
        when(skillExtractionService.extrairSkills(anyString()))
                .thenThrow(new AiException("falha", new RuntimeException()));

        mockMvc.perform(post("/api/skills/extract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricaoVaga\":\"qualquer coisa\"}"))
                .andExpect(status().isServiceUnavailable());
    }
}
