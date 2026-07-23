package com.david.matchcv.service;

import java.util.List;

import com.david.matchcv.domain.GapAnalyzer;
import com.david.matchcv.domain.ResultadoGap;
import com.david.matchcv.domain.SkillExtraida;
import com.david.matchcv.domain.SkillPrioridade;
import com.david.matchcv.domain.SkillPriorityCalculator;
import com.david.matchcv.dto.AnaliseCompletaResponse;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AnaliseCompletaService {

    private final SkillExtractionService skillExtractionService;
    private final SkillPriorityCalculator skillPriorityCalculator;
    private final CvAnalysisService cvAnalysisService;
    private final GapAnalyzer gapAnalyzer;

    public AnaliseCompletaService(SkillExtractionService skillExtractionService,
                                   SkillPriorityCalculator skillPriorityCalculator,
                                   CvAnalysisService cvAnalysisService,
                                   GapAnalyzer gapAnalyzer) {
        this.skillExtractionService = skillExtractionService;
        this.skillPriorityCalculator = skillPriorityCalculator;
        this.cvAnalysisService = cvAnalysisService;
        this.gapAnalyzer = gapAnalyzer;
    }

    /**
     * Orquestra a análise completa: extrai skills de cada vaga (IA), agrega e
     * prioriza (Java puro), extrai skills do CV (IA) e calcula o gap + sinergia
     * (Java puro). Cada etapa delega para o componente responsável.
     */
    public AnaliseCompletaResponse analisar(List<String> descricoesVagas, MultipartFile arquivo) {
        // 1. Extração por vaga: N chamadas de IA (Haiku), uma por descrição.
        List<List<SkillExtraida>> skillsPorVaga = descricoesVagas.stream()
                .map(skillExtractionService::extrairSkills)
                .toList();

        // 2. Agregação e priorização: Java puro.
        List<SkillPrioridade> skillsAgregadas = skillPriorityCalculator.calcular(skillsPorVaga);

        // 3. Extração do CV: 1 chamada de IA (mesmo prompt → vocabulário canônico consistente).
        List<SkillExtraida> skillsDoCv = cvAnalysisService.extrairSkillsDoCv(arquivo);

        // 4. Gap analysis + sinergia: Java puro.
        ResultadoGap gap = gapAnalyzer.analisar(skillsPorVaga, skillsDoCv);

        return new AnaliseCompletaResponse(
                descricoesVagas.size(),
                skillsAgregadas,
                gap.match(),
                gap.gap(),
                gap.extra(),
                gap.sinergiaMedia(),
                gap.vagasComSinergia());
    }
}
