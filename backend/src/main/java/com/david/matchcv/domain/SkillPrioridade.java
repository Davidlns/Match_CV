package com.david.matchcv.domain;

/**
 * Resultado da análise para uma skill: em quantas vagas aparece (frequencia),
 * em quantas é obrigatória (obrigatoriaEm), o percentual de vagas em que aparece
 * (percentual) e a prioridade calculada.
 */
public record SkillPrioridade(String nome, int frequencia, int obrigatoriaEm, int percentual, Prioridade prioridade) {
}
