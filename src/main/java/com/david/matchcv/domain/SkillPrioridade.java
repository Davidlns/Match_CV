package com.david.matchcv.domain;

/**
 * Resultado da análise para uma skill: em quantas vagas aparece (frequencia),
 * em quantas é obrigatória (obrigatoriaEm) e a prioridade calculada.
 */
public record SkillPrioridade(String nome, int frequencia, int obrigatoriaEm, Prioridade prioridade) {
}
