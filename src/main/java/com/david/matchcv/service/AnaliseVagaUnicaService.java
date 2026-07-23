package com.david.matchcv.service;

import java.io.IOException;
import java.util.List;

import com.david.matchcv.domain.GapAnalyzer;
import com.david.matchcv.domain.ResultadoGap;
import com.david.matchcv.domain.SkillExtraida;
import com.david.matchcv.dto.AnaliseVagaUnicaResponse;
import com.david.matchcv.exception.InvalidPdfException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AnaliseVagaUnicaService {

    private final PdfTextExtractor pdfTextExtractor;
    private final SkillExtractionService skillExtractionService;
    private final GapAnalyzer gapAnalyzer;
    private final VagaUnicaAiAnalyzer vagaUnicaAiAnalyzer;

    public AnaliseVagaUnicaService(PdfTextExtractor pdfTextExtractor,
            SkillExtractionService skillExtractionService,
            GapAnalyzer gapAnalyzer,
            VagaUnicaAiAnalyzer vagaUnicaAiAnalyzer) {
        this.pdfTextExtractor = pdfTextExtractor;
        this.skillExtractionService = skillExtractionService;
        this.gapAnalyzer = gapAnalyzer;
        this.vagaUnicaAiAnalyzer = vagaUnicaAiAnalyzer;
    }

    public AnaliseVagaUnicaResponse analisar(String descricaoVaga, MultipartFile arquivo) {
        byte[] bytes = lerArquivo(arquivo);
        String textoCv = pdfTextExtractor.extrairTexto(bytes);

        List<SkillExtraida> skillsVaga = skillExtractionService.extrairSkills(descricaoVaga);
        List<SkillExtraida> skillsDoCv = skillExtractionService.extrairSkills(textoCv);

        ResultadoGap gap = gapAnalyzer.analisar(List.of(skillsVaga), skillsDoCv);

        VagaUnicaAiAnalyzer.ResultadoIa resultadoIa = vagaUnicaAiAnalyzer.analisar(
                descricaoVaga, textoCv, gap.match(), gap.gap());

        return new AnaliseVagaUnicaResponse(
                gap.sinergiaMedia(),
                gap.match(),
                gap.gap(),
                gap.extra(),
                resultadoIa.roadmapDirecionado(),
                resultadoIa.feedbackAts());
    }

    private byte[] lerArquivo(MultipartFile arquivo) {
        if (arquivo.isEmpty()) throw new InvalidPdfException("Nenhum arquivo PDF foi enviado.");
        try {
            return arquivo.getBytes();
        } catch (IOException ex) {
            throw new InvalidPdfException("Não foi possível ler o arquivo enviado.", ex);
        }
    }
}
