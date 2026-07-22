package com.david.matchcv.service;

import static com.david.matchcv.domain.TipoSkill.DIFERENCIAL;
import static com.david.matchcv.domain.TipoSkill.OBRIGATORIA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import com.david.matchcv.domain.Prioridade;
import com.david.matchcv.domain.SkillExtraida;
import com.david.matchcv.domain.SkillPrioridade;
import com.david.matchcv.domain.SkillPriorityCalculator;
import com.david.matchcv.exception.AiException;

import org.junit.jupiter.api.Test;

class VagaAnalysisServiceTest {

    private final SkillExtractionService skillExtractionService = mock(SkillExtractionService.class);
    // Calculadora real: é lógica pura, não precisa de mock.
    private final VagaAnalysisService service =
            new VagaAnalysisService(skillExtractionService, new SkillPriorityCalculator());

    @Test
    void deveAgregarSkillsDeVariasVagas() {
        when(skillExtractionService.extrairSkills("vaga1")).thenReturn(List.of(
                new SkillExtraida("Java", OBRIGATORIA), new SkillExtraida("Docker", DIFERENCIAL)));
        when(skillExtractionService.extrairSkills("vaga2")).thenReturn(List.of(
                new SkillExtraida("Java", OBRIGATORIA)));

        List<SkillPrioridade> resultado = service.analisar(List.of("vaga1", "vaga2"));

        // Java: obrigatória em 2/2 (100%) -> ALTA. Docker: presente em 1/2 (50%) -> MEDIA.
        assertThat(resultado).containsExactly(
                new SkillPrioridade("Java", 2, 2, Prioridade.ALTA),
                new SkillPrioridade("Docker", 1, 0, Prioridade.MEDIA)
        );
    }

    @Test
    void devePropagarAiExceptionQuandoUmaExtracaoFalha() {
        when(skillExtractionService.extrairSkills(anyString()))
                .thenThrow(new AiException("falha", new RuntimeException()));

        assertThatThrownBy(() -> service.analisar(List.of("vaga1")))
                .isInstanceOf(AiException.class);
    }
}
