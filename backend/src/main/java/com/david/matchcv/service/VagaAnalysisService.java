package com.david.matchcv.service;

import java.util.List;

import com.david.matchcv.domain.SkillExtraida;
import com.david.matchcv.domain.SkillPrioridade;
import com.david.matchcv.domain.SkillPriorityCalculator;

import org.springframework.stereotype.Service;

@Service
public class VagaAnalysisService {

    private final SkillExtractionService skillExtractionService;
    private final SkillPriorityCalculator skillPriorityCalculator;

    public VagaAnalysisService(SkillExtractionService skillExtractionService,
                               SkillPriorityCalculator skillPriorityCalculator) {
        this.skillExtractionService = skillExtractionService;
        this.skillPriorityCalculator = skillPriorityCalculator;
    }

    public List<SkillPrioridade> analisar(List<String> descricoesVagas) {
        // 1. Extrai as skills (com tipo) de cada vaga (IA, reusando a Fatia 2).
        List<List<SkillExtraida>> skillsPorVaga = descricoesVagas.stream()
                .map(skillExtractionService::extrairSkills)
                .toList();

        // 2. Agrega e classifica (Java puro, sem IA).
        return skillPriorityCalculator.calcular(skillsPorVaga);
    }
}
