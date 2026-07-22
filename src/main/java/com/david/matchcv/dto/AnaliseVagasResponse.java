package com.david.matchcv.dto;

import java.util.List;

import com.david.matchcv.domain.SkillPrioridade;

// Saída da análise: quantas vagas foram analisadas e as skills priorizadas.
public record AnaliseVagasResponse(int totalVagas, List<SkillPrioridade> skills) {
}
