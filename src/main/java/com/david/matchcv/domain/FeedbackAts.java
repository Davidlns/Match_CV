package com.david.matchcv.domain;

import java.util.List;

/**
 * Resultado da simulação ATS para uma vaga específica.
 * pontuacaoEstimada — 0 a 100; reflete o alinhamento real, não um viés para inflar ou deflacionar.
 * pontosFavoraveis  — o que está funcionando bem no currículo para essa vaga.
 * pontosDeAtencao   — problemas genuínos; lista vazia se o currículo já está forte.
 * acoesPrioritarias — as ações de maior impacto (máximo 5); pode ser vazia em CV excelente.
 */
public record FeedbackAts(
        int pontuacaoEstimada,
        List<String> pontosFavoraveis,
        List<PontoDeAtencao> pontosDeAtencao,
        List<String> acoesPrioritarias) {
}
