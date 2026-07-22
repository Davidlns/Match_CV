package com.david.matchcv.dto;

import java.util.List;

// DTO de saída: a lista de skills normalizadas extraídas da vaga.
public record SkillsResponse(List<String> skills) {
}
