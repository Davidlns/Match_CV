package com.david.matchcv.dto;

import jakarta.validation.constraints.NotBlank;

// DTO de entrada do endpoint de extração. Record = forma enxuta e imutável de DTO.
public record ExtrairSkillsRequest(
        @NotBlank(message = "A descrição da vaga é obrigatória.") String descricaoVaga
) {
}
