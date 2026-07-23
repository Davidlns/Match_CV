package com.david.matchcv.dto;

import com.david.matchcv.domain.Prioridade;

import jakarta.validation.constraints.NotBlank;

public record SkillParaAprender(
        @NotBlank(message = "O nome da skill não pode ser vazio.")
        String nome,
        Prioridade prioridade) {
}
