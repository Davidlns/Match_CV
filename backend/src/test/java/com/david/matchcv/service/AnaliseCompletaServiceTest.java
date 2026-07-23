package com.david.matchcv.service;

import static com.david.matchcv.domain.TipoSkill.DIFERENCIAL;
import static com.david.matchcv.domain.TipoSkill.OBRIGATORIA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import com.david.matchcv.domain.GapAnalyzer;
import com.david.matchcv.domain.SkillExtraida;
import com.david.matchcv.domain.SkillPriorityCalculator;
import com.david.matchcv.domain.SinergiaVaga;
import com.david.matchcv.dto.AnaliseCompletaResponse;
import com.david.matchcv.exception.AiException;
import com.david.matchcv.exception.InvalidPdfException;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class AnaliseCompletaServiceTest {

    private final SkillExtractionService skillExtractionService = mock(SkillExtractionService.class);
    private final CvAnalysisService cvAnalysisService = mock(CvAnalysisService.class);
    // SkillPriorityCalculator e GapAnalyzer são lógica pura — usamos as instâncias reais.
    private final AnaliseCompletaService service = new AnaliseCompletaService(
            skillExtractionService,
            new SkillPriorityCalculator(),
            cvAnalysisService,
            new GapAnalyzer());

    @Test
    void deveOrcuestrarTodasAsEtapas() {
        when(skillExtractionService.extrairSkills("vaga1")).thenReturn(
                List.of(new SkillExtraida("Java", OBRIGATORIA),
                        new SkillExtraida("Docker", DIFERENCIAL)));

        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo", "cv.pdf", "application/pdf", "%PDF".getBytes());
        when(cvAnalysisService.extrairSkillsDoCv(any())).thenReturn(
                List.of(new SkillExtraida("Java", OBRIGATORIA),
                        new SkillExtraida("Python", OBRIGATORIA)));

        AnaliseCompletaResponse resultado = service.analisar(List.of("vaga1"), arquivo);

        assertThat(resultado.totalVagas()).isEqualTo(1);
        assertThat(resultado.match()).containsExactly("Java");
        assertThat(resultado.gap()).containsExactly("Docker");
        assertThat(resultado.extra()).containsExactly("Python");
        // 1 de 2 skills da vaga coberta pelo CV → sinergia 50%
        assertThat(resultado.vagasComSinergia()).containsExactly(new SinergiaVaga(1, 50));
        assertThat(resultado.sinergiaMedia()).isEqualTo(50);
    }

    @Test
    void devePropagArAiExceptionDaExtracaoDaVaga() {
        when(skillExtractionService.extrairSkills(anyString()))
                .thenThrow(new AiException("falha", new RuntimeException()));

        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo", "cv.pdf", "application/pdf", "%PDF".getBytes());

        assertThatThrownBy(() -> service.analisar(List.of("vaga1"), arquivo))
                .isInstanceOf(AiException.class);
    }

    @Test
    void devePropagArInvalidPdfExcecaoDoCv() {
        when(skillExtractionService.extrairSkills(anyString())).thenReturn(List.of());
        when(cvAnalysisService.extrairSkillsDoCv(any()))
                .thenThrow(new InvalidPdfException("PDF corrompido."));

        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo", "nao.txt", "text/plain", "nao e pdf".getBytes());

        assertThatThrownBy(() -> service.analisar(List.of("vaga1"), arquivo))
                .isInstanceOf(InvalidPdfException.class);
    }
}
