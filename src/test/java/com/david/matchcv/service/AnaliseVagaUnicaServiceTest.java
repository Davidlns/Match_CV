package com.david.matchcv.service;

import static com.david.matchcv.domain.TipoSkill.DIFERENCIAL;
import static com.david.matchcv.domain.TipoSkill.OBRIGATORIA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import com.david.matchcv.domain.FeedbackAts;
import com.david.matchcv.domain.GapAnalyzer;
import com.david.matchcv.domain.SkillExtraida;
import com.david.matchcv.dto.AnaliseVagaUnicaResponse;
import com.david.matchcv.exception.AiException;
import com.david.matchcv.exception.InvalidPdfException;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class AnaliseVagaUnicaServiceTest {

    private final PdfTextExtractor pdfTextExtractor = mock(PdfTextExtractor.class);
    private final SkillExtractionService skillExtractionService = mock(SkillExtractionService.class);
    private final VagaUnicaAiAnalyzer vagaUnicaAiAnalyzer = mock(VagaUnicaAiAnalyzer.class);
    // GapAnalyzer é lógica pura — usamos a instância real para cobrir o cruzamento corretamente.
    private final AnaliseVagaUnicaService service = new AnaliseVagaUnicaService(
            pdfTextExtractor,
            skillExtractionService,
            new GapAnalyzer(),
            vagaUnicaAiAnalyzer);

    @Test
    void deveOrcuestrarTodasAsEtapasECalcularSinergiaCorretamente() {
        when(pdfTextExtractor.extrairTexto(any())).thenReturn("texto do cv");
        when(skillExtractionService.extrairSkills("descricao da vaga")).thenReturn(
                List.of(new SkillExtraida("Java", OBRIGATORIA),
                        new SkillExtraida("Docker", DIFERENCIAL)));
        when(skillExtractionService.extrairSkills("texto do cv")).thenReturn(
                List.of(new SkillExtraida("Java", OBRIGATORIA),
                        new SkillExtraida("Python", OBRIGATORIA)));

        FeedbackAts feedbackAts = new FeedbackAts(80, List.of("Boa estrutura"), List.of(), List.of());
        VagaUnicaAiAnalyzer.ResultadoIa resultadoIa = new VagaUnicaAiAnalyzer.ResultadoIa("## Fase 1", feedbackAts);
        when(vagaUnicaAiAnalyzer.analisar(anyString(), anyString(), anyList(), anyList()))
                .thenReturn(resultadoIa);

        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo", "cv.pdf", "application/pdf", "%PDF".getBytes());

        AnaliseVagaUnicaResponse resultado = service.analisar("descricao da vaga", arquivo);

        assertThat(resultado.sinergia()).isEqualTo(50); // 1 de 2 skills da vaga coberta
        assertThat(resultado.match()).containsExactly("Java");
        assertThat(resultado.gap()).containsExactly("Docker");
        assertThat(resultado.extra()).containsExactly("Python");
        assertThat(resultado.roadmapDirecionado()).isEqualTo("## Fase 1");
        assertThat(resultado.feedbackAts().pontuacaoEstimada()).isEqualTo(80);
    }

    @Test
    void deveRetornarSinergia100QuandoCvCobreTodaAVaga() {
        when(pdfTextExtractor.extrairTexto(any())).thenReturn("texto do cv");
        when(skillExtractionService.extrairSkills("vaga")).thenReturn(
                List.of(new SkillExtraida("Java", OBRIGATORIA)));
        when(skillExtractionService.extrairSkills("texto do cv")).thenReturn(
                List.of(new SkillExtraida("Java", OBRIGATORIA)));

        FeedbackAts feedbackAts = new FeedbackAts(95, List.of("Excelente alinhamento"), List.of(), List.of());
        when(vagaUnicaAiAnalyzer.analisar(anyString(), anyString(), anyList(), anyList()))
                .thenReturn(new VagaUnicaAiAnalyzer.ResultadoIa(
                        "Você já possui as skills necessárias para esta vaga.", feedbackAts));

        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo", "cv.pdf", "application/pdf", "%PDF".getBytes());

        AnaliseVagaUnicaResponse resultado = service.analisar("vaga", arquivo);

        assertThat(resultado.sinergia()).isEqualTo(100);
        assertThat(resultado.gap()).isEmpty();
        assertThat(resultado.match()).containsExactly("Java");
    }

    @Test
    void devePropagArInvalidPdfException() {
        when(pdfTextExtractor.extrairTexto(any()))
                .thenThrow(new InvalidPdfException("PDF inválido ou corrompido."));

        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo", "cv.txt", "text/plain", "nao e pdf".getBytes());

        assertThatThrownBy(() -> service.analisar("vaga", arquivo))
                .isInstanceOf(InvalidPdfException.class);
    }

    @Test
    void devePropagArAiException() {
        when(pdfTextExtractor.extrairTexto(any())).thenReturn("texto do cv");
        when(skillExtractionService.extrairSkills(anyString())).thenReturn(List.of());
        when(vagaUnicaAiAnalyzer.analisar(any(), any(), any(), any()))
                .thenThrow(new AiException("falha na IA", new RuntimeException()));

        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo", "cv.pdf", "application/pdf", "%PDF".getBytes());

        assertThatThrownBy(() -> service.analisar("vaga", arquivo))
                .isInstanceOf(AiException.class);
    }

    @Test
    void deveRejeitarArquivoVazio() {
        MockMultipartFile arquivoVazio = new MockMultipartFile(
                "arquivo", "cv.pdf", "application/pdf", new byte[0]);

        assertThatThrownBy(() -> service.analisar("vaga", arquivoVazio))
                .isInstanceOf(InvalidPdfException.class)
                .hasMessageContaining("Nenhum arquivo PDF foi enviado.");
    }
}
