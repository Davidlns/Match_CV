package com.david.matchcv.domain;

// Uma skill extraída de uma vaga, com o tipo (obrigatória ou diferencial).
public record SkillExtraida(String nome, TipoSkill tipo) {
}
