package com.david.matchcv.domain;

public record PontoDeAtencao(
        CategoriaAtencao categoria,
        String descricao,
        ImpactoAtencao impacto) {
}
