package com.david.matchcv.domain;

import static com.david.matchcv.domain.TipoSkill.DIFERENCIAL;
import static com.david.matchcv.domain.TipoSkill.OBRIGATORIA;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class GapAnalyzerTest {

    private final GapAnalyzer gapAnalyzer = new GapAnalyzer();

    @Test
    void deveClassificarMatchGapEExtra() {
        List<List<SkillExtraida>> skillsPorVaga = List.of(
                List.of(new SkillExtraida("Java", OBRIGATORIA),
                        new SkillExtraida("Docker", OBRIGATORIA),
                        new SkillExtraida("Kubernetes", DIFERENCIAL))
        );
        List<SkillExtraida> skillsDoCv = List.of(
                new SkillExtraida("Java", OBRIGATORIA),
                new SkillExtraida("Python", OBRIGATORIA)
        );

        ResultadoGap resultado = gapAnalyzer.analisar(skillsPorVaga, skillsDoCv);

        assertThat(resultado.match()).containsExactly("Java");
        assertThat(resultado.gap()).containsExactlyInAnyOrder("Docker", "Kubernetes");
        assertThat(resultado.extra()).containsExactly("Python");
    }

    @Test
    void devePropagArSinergiaCorretamentePorVaga() {
        // vaga1: Java + Docker (2 skills) — CV tem Java → sinergia 50%
        // vaga2: Java + Docker + Spring Boot (3 skills) — CV tem Java + Spring Boot → sinergia 66%
        List<List<SkillExtraida>> skillsPorVaga = List.of(
                List.of(new SkillExtraida("Java", OBRIGATORIA),
                        new SkillExtraida("Docker", OBRIGATORIA)),
                List.of(new SkillExtraida("Java", OBRIGATORIA),
                        new SkillExtraida("Docker", OBRIGATORIA),
                        new SkillExtraida("Spring Boot", OBRIGATORIA))
        );
        List<SkillExtraida> skillsDoCv = List.of(
                new SkillExtraida("Java", OBRIGATORIA),
                new SkillExtraida("Spring Boot", OBRIGATORIA)
        );

        ResultadoGap resultado = gapAnalyzer.analisar(skillsPorVaga, skillsDoCv);

        // vaga1: 1/2 = 50%;  vaga2: 2/3 = 66% (inteiro truncado)
        assertThat(resultado.vagasComSinergia()).containsExactly(
                new SinergiaVaga(1, 50),
                new SinergiaVaga(2, 66)
        );
        // (50 + 66) / 2.0 = 58.0 → cast para int = 58
        assertThat(resultado.sinergiaMedia()).isEqualTo(58);
    }

    @Test
    void deveRetornarGapTotalQuandoCvVazio() {
        List<List<SkillExtraida>> skillsPorVaga = List.of(
                List.of(new SkillExtraida("Java", OBRIGATORIA),
                        new SkillExtraida("Docker", OBRIGATORIA))
        );

        ResultadoGap resultado = gapAnalyzer.analisar(skillsPorVaga, List.of());

        assertThat(resultado.match()).isEmpty();
        assertThat(resultado.gap()).containsExactlyInAnyOrder("Java", "Docker");
        assertThat(resultado.extra()).isEmpty();
        assertThat(resultado.vagasComSinergia()).containsExactly(new SinergiaVaga(1, 0));
        assertThat(resultado.sinergiaMedia()).isZero();
    }

    @Test
    void deveIgnorarCaseDiferenteNaComparacao() {
        // A IA normaliza, mas como segurança extra comparamos case-insensitive.
        List<List<SkillExtraida>> skillsPorVaga = List.of(
                List.of(new SkillExtraida("Java", OBRIGATORIA))
        );
        List<SkillExtraida> skillsDoCv = List.of(
                new SkillExtraida("java", OBRIGATORIA) // minúsculo no CV
        );

        ResultadoGap resultado = gapAnalyzer.analisar(skillsPorVaga, skillsDoCv);

        // Deve ser match; o nome exibido vem da vaga (canônico).
        assertThat(resultado.match()).containsExactly("Java");
        assertThat(resultado.gap()).isEmpty();
    }

    @Test
    void deveDeduplcarSkillsDaMesmaVagaNaSinergia() {
        // "Java" aparece duas vezes na vaga → conta 1 skill única ao calcular sinergia.
        List<List<SkillExtraida>> skillsPorVaga = List.of(
                List.of(new SkillExtraida("Java", OBRIGATORIA),
                        new SkillExtraida("Java", DIFERENCIAL),
                        new SkillExtraida("Docker", OBRIGATORIA))
        );
        List<SkillExtraida> skillsDoCv = List.of(
                new SkillExtraida("Java", OBRIGATORIA)
        );

        ResultadoGap resultado = gapAnalyzer.analisar(skillsPorVaga, skillsDoCv);

        // 2 skills únicas (Java, Docker); CV cobre 1 → 50%
        assertThat(resultado.vagasComSinergia()).containsExactly(new SinergiaVaga(1, 50));
    }
}
