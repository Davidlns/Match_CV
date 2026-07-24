package com.david.matchcv.dto;

import java.util.List;

import com.david.matchcv.domain.SkillAgregada;

// Saída da análise: quantas vagas foram analisadas e as skills agregadas.
public record AnaliseVagasResponse(int totalVagas, List<SkillAgregada> skills) {
}
