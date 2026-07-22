package com.david.matchcv.domain;

import static com.david.matchcv.domain.TipoSkill.DIFERENCIAL;
import static com.david.matchcv.domain.TipoSkill.OBRIGATORIA;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class SkillPriorityCalculatorTest {

    private final SkillPriorityCalculator calculator = new SkillPriorityCalculator();

    @Test
    void deveCombinarTipoEFrequenciaEntreVagas() {
        List<List<SkillExtraida>> skillsPorVaga = List.of(
                List.of(new SkillExtraida("Java", OBRIGATORIA), new SkillExtraida("Docker", OBRIGATORIA),
                        new SkillExtraida("Kafka", DIFERENCIAL)),
                List.of(new SkillExtraida("Java", OBRIGATORIA), new SkillExtraida("Docker", DIFERENCIAL),
                        new SkillExtraida("Kubernetes", OBRIGATORIA)),
                List.of(new SkillExtraida("Java", OBRIGATORIA))
        );

        List<SkillPrioridade> resultado = calculator.calcular(skillsPorVaga);

        // Java: obrigatória em 3/3 (100%) -> ALTA.
        // Docker: presente em 2/3 (66%), obrigatória em 1/3 (33%) -> MEDIA.
        // Kafka: presente em 1/3 (33%) -> BAIXA.  Kubernetes: obrigatória em 1/3 (33%) -> BAIXA.
        assertThat(resultado).containsExactly(
                new SkillPrioridade("Java", 3, 3, Prioridade.ALTA),
                new SkillPrioridade("Docker", 2, 1, Prioridade.MEDIA),
                new SkillPrioridade("Kafka", 1, 0, Prioridade.BAIXA),
                new SkillPrioridade("Kubernetes", 1, 1, Prioridade.BAIXA)
        );
    }

    @Test
    void deveClassificarComUmaUnicaVagaPeloTipo() {
        List<List<SkillExtraida>> skillsPorVaga = List.of(
                List.of(new SkillExtraida("Java", OBRIGATORIA), new SkillExtraida("Kafka", DIFERENCIAL))
        );

        List<SkillPrioridade> resultado = calculator.calcular(skillsPorVaga);

        // Com 1 vaga: obrigatória -> ALTA; diferencial -> MEDIA.
        assertThat(resultado).containsExactly(
                new SkillPrioridade("Java", 1, 1, Prioridade.ALTA),
                new SkillPrioridade("Kafka", 1, 0, Prioridade.MEDIA)
        );
    }

    @Test
    void deveColapsarPorVagaConsiderandoObrigatoriaQuandoHaConflito() {
        // "Java" aparece como diferencial e obrigatória na mesma vaga -> conta como obrigatória.
        List<List<SkillExtraida>> skillsPorVaga = List.of(
                List.of(new SkillExtraida("Java", DIFERENCIAL), new SkillExtraida("Java", OBRIGATORIA)),
                List.of(new SkillExtraida("Java", OBRIGATORIA))
        );

        List<SkillPrioridade> resultado = calculator.calcular(skillsPorVaga);

        assertThat(resultado).containsExactly(
                new SkillPrioridade("Java", 2, 2, Prioridade.ALTA)
        );
    }

    @Test
    void deveRetornarVazioSemVagas() {
        assertThat(calculator.calcular(List.of())).isEmpty();
    }
}
