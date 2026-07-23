package com.david.matchcv.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.david.matchcv.exception.AiException;
import com.david.matchcv.service.RoadmapService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RoadmapController.class)
class RoadmapControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoadmapService roadmapService;

    @Test
    void deveRetornar200ComRoadmap() throws Exception {
        when(roadmapService.gerarRoadmap(any())).thenReturn("## Fase 1\n- Aprenda Java");

        mockMvc.perform(post("/api/roadmap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "skills": [
                                    {"nome": "Java", "prioridade": "ALTA"},
                                    {"nome": "Docker", "prioridade": "MEDIA"}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roadmap").value("## Fase 1\n- Aprenda Java"));
    }

    @Test
    void deveRetornar400QuandoListaVazia() throws Exception {
        mockMvc.perform(post("/api/roadmap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skills\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void deveRetornar400QuandoNomeDaSkillVazio() throws Exception {
        mockMvc.perform(post("/api/roadmap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skills\":[{\"nome\":\"\",\"prioridade\":\"ALTA\"}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void deveRetornar503QuandoAIaFalha() throws Exception {
        when(roadmapService.gerarRoadmap(any()))
                .thenThrow(new AiException("falha", new RuntimeException()));

        mockMvc.perform(post("/api/roadmap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"skills":[{"nome":"Java","prioridade":"ALTA"}]}
                                """))
                .andExpect(status().isServiceUnavailable());
    }
}
