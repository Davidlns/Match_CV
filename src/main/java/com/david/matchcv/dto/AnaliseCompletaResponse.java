package com.david.matchcv.dto;

import java.util.List;

import com.david.matchcv.domain.SkillPrioridade;
import com.david.matchcv.domain.SinergiaVaga;

public record AnaliseCompletaResponse(
        int totalVagas,
        List<SkillPrioridade> skillsAgregadas,
        List<String> match,
        List<String> gap,
        List<String> extra,
        int sinergiaMedia,
        List<SinergiaVaga> vagasComSinergia) {
}
