package com.david.matchcv.domain;

/**
 * Skill agregada entre as vagas, com o dado bruto e o estrato de consenso.
 * frequencia    — em quantas vagas aparece.
 * obrigatoriaEm — em quantas é obrigatória.
 * percentual    — % de vagas em que aparece.
 * estrato       — faixa de densidade de presença (ver EstratoConsenso).
 */
public record SkillAgregada(
        String nome,
        int frequencia,
        int obrigatoriaEm,
        int percentual,
        EstratoConsenso estrato) {
}
