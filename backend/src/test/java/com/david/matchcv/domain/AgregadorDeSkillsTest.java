package com.david.matchcv.domain;

import static com.david.matchcv.domain.EstratoConsenso.FREQUENTE;
import static com.david.matchcv.domain.EstratoConsenso.PONTUAL;
import static com.david.matchcv.domain.EstratoConsenso.PRATICAMENTE_TODAS;
import static com.david.matchcv.domain.TipoSkill.DIFERENCIAL;
import static com.david.matchcv.domain.TipoSkill.OBRIGATORIA;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class AgregadorDeSkillsTest {

    private final AgregadorDeSkills agregador = new AgregadorDeSkills();

    @Test
    void deveAgregarOrdenarPorPesoEEstratificar() {
        List<List<SkillExtraida>> skillsPorVaga = List.of(
                List.of(new SkillExtraida("Java", OBRIGATORIA), new SkillExtraida("Docker", OBRIGATORIA),
                        new SkillExtraida("Kafka", DIFERENCIAL)),
                List.of(new SkillExtraida("Java", OBRIGATORIA), new SkillExtraida("Docker", DIFERENCIAL),
                        new SkillExtraida("Spring", OBRIGATORIA)),
                List.of(new SkillExtraida("Java", OBRIGATORIA), new SkillExtraida("Spring", OBRIGATORIA),
                        new SkillExtraida("AWS", DIFERENCIAL)),
                List.of(new SkillExtraida("Java", OBRIGATORIA), new SkillExtraida("Spring", OBRIGATORIA))
        );

        List<SkillAgregada> resultado = agregador.agregar(skillsPorVaga);

        // Java   4/4 (100%, peso 8) -> PRATICAMENTE_TODAS
        // Spring 3/4 (75%,  peso 6) -> FREQUENTE
        // Docker 2/4 (50%,  peso 3) -> FREQUENTE
        // AWS/Kafka 1/4 (25%, peso 1) -> PONTUAL; empate resolvido por nome (AWS < Kafka)
        assertThat(resultado).containsExactly(
                new SkillAgregada("Java", 4, 4, 100, PRATICAMENTE_TODAS),
                new SkillAgregada("Spring", 3, 3, 75, FREQUENTE),
                new SkillAgregada("Docker", 2, 1, 50, FREQUENTE),
                new SkillAgregada("AWS", 1, 0, 25, PONTUAL),
                new SkillAgregada("Kafka", 1, 0, 25, PONTUAL)
        );
    }

    @Test
    void obrigatoriaDeveTerMaisPesoQueDiferencialNaMesmaFrequencia() {
        // A e B aparecem ambas em 3/4 vagas; A sempre obrigatória, B sempre diferencial.
        List<List<SkillExtraida>> skillsPorVaga = List.of(
                List.of(new SkillExtraida("A", OBRIGATORIA), new SkillExtraida("B", DIFERENCIAL)),
                List.of(new SkillExtraida("A", OBRIGATORIA), new SkillExtraida("B", DIFERENCIAL)),
                List.of(new SkillExtraida("A", OBRIGATORIA), new SkillExtraida("B", DIFERENCIAL)),
                List.of(new SkillExtraida("C", OBRIGATORIA))
        );

        List<SkillAgregada> resultado = agregador.agregar(skillsPorVaga);

        // A (peso 6) vem antes de B (peso 3), mesmo com a mesma frequência.
        assertThat(resultado).containsExactly(
                new SkillAgregada("A", 3, 3, 75, FREQUENTE),
                new SkillAgregada("B", 3, 0, 75, FREQUENTE),
                new SkillAgregada("C", 1, 1, 25, PONTUAL)
        );
    }

    @Test
    void deveRespeitarOsLimiaresDeEstratoNasFronteiras() {
        // 5 vagas: X em 4/5 (80% — fronteira PRATICAMENTE_TODAS),
        //          Y em 2/5 (40% — fronteira FREQUENTE), Z em 1/5 (20% — PONTUAL).
        List<List<SkillExtraida>> skillsPorVaga = List.of(
                List.of(new SkillExtraida("X", OBRIGATORIA), new SkillExtraida("Y", OBRIGATORIA)),
                List.of(new SkillExtraida("X", OBRIGATORIA), new SkillExtraida("Y", OBRIGATORIA)),
                List.of(new SkillExtraida("X", OBRIGATORIA)),
                List.of(new SkillExtraida("X", OBRIGATORIA)),
                List.of(new SkillExtraida("Z", DIFERENCIAL))
        );

        List<SkillAgregada> resultado = agregador.agregar(skillsPorVaga);

        assertThat(resultado).containsExactly(
                new SkillAgregada("X", 4, 4, 80, PRATICAMENTE_TODAS),
                new SkillAgregada("Y", 2, 2, 40, FREQUENTE),
                new SkillAgregada("Z", 1, 0, 20, PONTUAL)
        );
    }

    @Test
    void deveColapsarPorVagaConsiderandoObrigatoriaQuandoHaConflito() {
        // "Java" aparece como diferencial e obrigatória na mesma vaga -> conta como obrigatória.
        List<List<SkillExtraida>> skillsPorVaga = List.of(
                List.of(new SkillExtraida("Java", DIFERENCIAL), new SkillExtraida("Java", OBRIGATORIA)),
                List.of(new SkillExtraida("Java", OBRIGATORIA))
        );

        List<SkillAgregada> resultado = agregador.agregar(skillsPorVaga);

        assertThat(resultado).containsExactly(
                new SkillAgregada("Java", 2, 2, 100, PRATICAMENTE_TODAS)
        );
    }

    @Test
    void comUmaUnicaVagaTudoFicaEmPraticamenteTodas() {
        // Caso degenerado (barrado pelos limites 3–8 na Fatia 2): tudo é 100%.
        List<List<SkillExtraida>> skillsPorVaga = List.of(
                List.of(new SkillExtraida("Java", OBRIGATORIA), new SkillExtraida("Kafka", DIFERENCIAL))
        );

        List<SkillAgregada> resultado = agregador.agregar(skillsPorVaga);

        assertThat(resultado).containsExactly(
                new SkillAgregada("Java", 1, 1, 100, PRATICAMENTE_TODAS),
                new SkillAgregada("Kafka", 1, 0, 100, PRATICAMENTE_TODAS)
        );
    }

    @Test
    void deveRetornarVazioSemVagas() {
        assertThat(agregador.agregar(List.of())).isEmpty();
    }
}
