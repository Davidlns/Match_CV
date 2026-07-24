package com.david.matchcv.service;

import java.util.List;

import com.david.matchcv.domain.AgregadorDeSkills;
import com.david.matchcv.domain.SkillAgregada;
import com.david.matchcv.domain.SkillExtraida;

import org.springframework.stereotype.Service;

@Service
public class VagaAnalysisService {

    private final SkillExtractionService skillExtractionService;
    private final AgregadorDeSkills agregadorDeSkills;

    public VagaAnalysisService(SkillExtractionService skillExtractionService,
                               AgregadorDeSkills agregadorDeSkills) {
        this.skillExtractionService = skillExtractionService;
        this.agregadorDeSkills = agregadorDeSkills;
    }

    public List<SkillAgregada> analisar(List<String> descricoesVagas) {
        // 1. Extrai as skills (com tipo) de cada vaga (IA, reusando a Fatia 2).
        List<List<SkillExtraida>> skillsPorVaga = descricoesVagas.stream()
                .map(skillExtractionService::extrairSkills)
                .toList();

        // 2. Agrega, ordena e estratifica (Java puro, sem IA).
        return agregadorDeSkills.agregar(skillsPorVaga);
    }
}
