package com.david.matchcv.dto;

import java.util.List;

import com.david.matchcv.domain.SkillExtraida;

// Saída da extração de uma vaga: as skills com seus tipos (obrigatória/diferencial).
public record SkillsResponse(List<SkillExtraida> skills) {
}
