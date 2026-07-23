package com.david.matchcv.service;

import static com.david.matchcv.domain.TipoSkill.OBRIGATORIA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import com.david.matchcv.domain.SkillExtraida;
import com.david.matchcv.exception.InvalidPdfException;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class CvAnalysisServiceTest {

    private final PdfTextExtractor pdfTextExtractor = mock(PdfTextExtractor.class);
    private final SkillExtractionService skillExtractionService = mock(SkillExtractionService.class);
    private final CvAnalysisService service =
            new CvAnalysisService(pdfTextExtractor, skillExtractionService);

    @Test
    void deveExtrairSkillsDeUmPdfValido() {
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo", "cv.pdf", "application/pdf", "%PDF conteudo".getBytes());

        when(pdfTextExtractor.extrairTexto(any(byte[].class))).thenReturn("Java Spring Boot");
        when(skillExtractionService.extrairSkills("Java Spring Boot"))
                .thenReturn(List.of(new SkillExtraida("Java", OBRIGATORIA)));

        List<SkillExtraida> resultado = service.extrairSkillsDoCv(arquivo);

        assertThat(resultado).containsExactly(new SkillExtraida("Java", OBRIGATORIA));
    }

    @Test
    void deveLancarExcecaoParaArquivoVazio() {
        MockMultipartFile arquivo = new MockMultipartFile("arquivo", new byte[0]);

        assertThatThrownBy(() -> service.extrairSkillsDoCv(arquivo))
                .isInstanceOf(InvalidPdfException.class)
                .hasMessageContaining("Nenhum arquivo");
    }

    @Test
    void devePropagArInvalidPdfExcecaoQuandoPdfCorrempido() {
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo", "cv.txt", "text/plain", "nao e um pdf".getBytes());

        when(pdfTextExtractor.extrairTexto(any(byte[].class)))
                .thenThrow(new InvalidPdfException("corrompido", new IOException()));

        assertThatThrownBy(() -> service.extrairSkillsDoCv(arquivo))
                .isInstanceOf(InvalidPdfException.class);
    }
}
