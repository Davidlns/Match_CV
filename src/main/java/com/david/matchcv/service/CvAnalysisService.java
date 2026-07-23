package com.david.matchcv.service;

import java.io.IOException;
import java.util.List;

import com.david.matchcv.domain.SkillExtraida;
import com.david.matchcv.exception.InvalidPdfException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CvAnalysisService {

    private final PdfTextExtractor pdfTextExtractor;
    private final SkillExtractionService skillExtractionService;

    public CvAnalysisService(PdfTextExtractor pdfTextExtractor,
                              SkillExtractionService skillExtractionService) {
        this.pdfTextExtractor = pdfTextExtractor;
        this.skillExtractionService = skillExtractionService;
    }

    /**
     * Recebe o PDF do currículo como MultipartFile (já em memória pelo Spring),
     * extrai o texto via PDFBox e extrai as skills com a IA.
     * O conteúdo nunca é gravado em disco.
     */
    public List<SkillExtraida> extrairSkillsDoCv(MultipartFile arquivo) {
        if (arquivo.isEmpty()) {
            throw new InvalidPdfException("Nenhum arquivo PDF foi enviado.");
        }
        try {
            byte[] bytes = arquivo.getBytes();
            String texto = pdfTextExtractor.extrairTexto(bytes);
            return skillExtractionService.extrairSkills(texto);
        } catch (InvalidPdfException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new InvalidPdfException("Não foi possível ler o arquivo enviado.", ex);
        }
    }
}
